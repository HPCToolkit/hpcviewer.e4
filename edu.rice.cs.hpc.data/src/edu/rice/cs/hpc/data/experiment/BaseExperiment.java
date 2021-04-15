package edu.rice.cs.hpc.data.experiment;

import java.io.File;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

import edu.rice.cs.hpc.data.experiment.scope.ITraceScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.visitors.DisposeResourcesVisitor;
import edu.rice.cs.hpc.data.experiment.scope.visitors.FilterScopeVisitor;
import edu.rice.cs.hpc.data.filter.IFilterData;
import edu.rice.cs.hpc.data.trace.BaseTraceAttribute;
import edu.rice.cs.hpc.data.trace.TraceAttribute;
import edu.rice.cs.hpc.data.util.IUserData;


/***
 * Base abstract experiment that handle only call path.
 * 
 * No metric is associated in this experiment
 *
 */
public abstract class BaseExperiment implements IExperiment 
{
	static public final int DB_SUMMARY_INDEX = 0;
	static public final int DB_SUMMARY_TRACE = 1;
	static public final int DB_SUMMARY_PLOT = 2;
	static public final int DB_SUMMARY_THREAD = 3;
	
	/*****
	 *  Enumeration for database file type
	 */
	static public enum Db_File_Type {DB_SUMMARY, DB_TRACE, DB_PLOT, DB_THREADS};
	
	static final private String []DefaultDbFilename = {"profile.db", "trace.db", "cct.db", "profile.db"};
	
	/** The experiment's configuration. */
	protected ExperimentConfiguration configuration;

	protected RootScope rootScope;
	
	protected RootScope datacentricRootScope;
	
	/** version of the database **/
	private short versionMajor, versionMinor;

	protected IDatabaseRepresentation databaseRepresentation;
	
	private EnumMap<Db_File_Type, String> db_filenames;
	private int filterNumScopes = 0, filterStatus;

	private BaseTraceAttribute traceAttribute = new TraceAttribute();

	/***
	 * the root scope of the experiment
	 * 
	 * @param the root scope
	 */
	public void setRootScope(Scope rootScope) {
		this.rootScope = (RootScope) rootScope;
	}

	
	/***
	 * retrieve the root scope.
	 * 
	 * @return the root scope
	 */
	@Override
	public Scope getRootScope() {
		return rootScope;
	}

	
	public void setDatacentricRootScope(RootScope rootScope) {
		this.datacentricRootScope = rootScope;
	}
	
	
	public RootScope getDatacentricRootScope() {
		return datacentricRootScope;
	}
	
	
	
	/******
	 * set a database filename
	 * 
	 * @param file_index : enumerate database filename {@link Db_File_Type}
	 * @param filename : the name of the file
	 */
	public void setDBFilename(Db_File_Type file_index, String filename)
	{
		if (db_filenames == null)
		{
			db_filenames = new EnumMap<Db_File_Type, String>(Db_File_Type.class);
		}
		db_filenames.put(file_index, filename);
	}
	
	/****
	 * get the database file name 
	 * @param file_index : enumerate database filename {@link Db_File_Type}
	 * @return String file name
	 */
	public String getDbFilename(Db_File_Type file_index)
	{
		if (db_filenames == null)
		{
			db_filenames = new EnumMap<BaseExperiment.Db_File_Type, String>(Db_File_Type.class);
			db_filenames.put(Db_File_Type.DB_SUMMARY, DefaultDbFilename[DB_SUMMARY_INDEX]);
			db_filenames.put(Db_File_Type.DB_TRACE,   DefaultDbFilename[DB_SUMMARY_TRACE]);
			db_filenames.put(Db_File_Type.DB_PLOT,    DefaultDbFilename[DB_SUMMARY_PLOT]);
			db_filenames.put(Db_File_Type.DB_THREADS, DefaultDbFilename[DB_SUMMARY_THREAD]);
		}
		return db_filenames.get(file_index);
	}
	
	public int getMajorVersion()
	{
		return versionMajor;
	}

	public int getMinorVersion() 
	{
		return versionMinor;
	}
	

	/**
	 * @return the maxDepth
	 */
	public int getMaxDepth() {
		return traceAttribute.maxDepth;
	}


	/**
	 * @param maxDepth the maxDepth to set
	 */
	public void setMaxDepth(int maxDepth) {
		traceAttribute.maxDepth = maxDepth;
	}


	/**
	 * @return the scopeMap
	 */
	public Map<Integer, ITraceScope> getScopeMap() {
		return traceAttribute.mapCpidToCallpath;
	}


	/**
	 * @param scopeMap the scopeMap to set
	 */
	public void setScopeMap(Map<Integer, ITraceScope> scopeMap) {
		traceAttribute.mapCpidToCallpath = scopeMap;
	}




	static public String getDefaultDatabaseName(Db_File_Type type)
	{
		return DefaultDbFilename[type.ordinal()];
	}
	
	static public String getDefaultDbTraceFilename()
	{
		return getDefaultDatabaseName(Db_File_Type.DB_TRACE);
	}


	public Object[] getRootScopeChildren() {
		RootScope root = (RootScope) getRootScope();

		if (root != null)
			return root.getChildren();
		else
			return null;
	}
	
	/****
	 * Retrieve the root scope of this experiment based on the root type
	 * (cct, callers tree or flat tree)
	 * 
	 * @param type : RootScopeType, type of the root
	 * @return the root scope if the type is found, <code>null</code> otherwise
	 */
	public RootScope getRootScope(RootScopeType type)
	{
		RootScope root = (RootScope) getRootScope();
		for (int i=0; i<root.getChildCount(); i++)
		{
			if (((RootScope)root.getChildAt(i)).getType() == type)
				return (RootScope) root.getChildAt(i);
		}
		return null;
	}
	
	/****
	 * open a local database
	 * 
	 * @param fileExperiment : file of the experiment xml
	 * @param userData : map of user preferences
	 * @param need_metric : whether we need to assign metrics or not
	 * @throws Exception
	 */
	public void open(File fileExperiment, IUserData<String, String> userData, boolean need_metric)
			throws	Exception
	{
		databaseRepresentation = new LocalDatabaseRepresentation(fileExperiment, userData, need_metric);
		databaseRepresentation.open(this);
		open_finalize();
	}
	
	
	/******
	 * This method is used for opening XML from a remote machine
	 *  
	 * @param expStream : remote input stream
	 * @param userData : customized user data
	 * @param name : the remote directory
	 * @throws Exception 
	 *****/
	public void open(InputStream expStream, IUserData<String, String> userData,
		String name) throws Exception {
		databaseRepresentation = new RemoteDatabaseRepresentation(expStream, userData, name);
		databaseRepresentation.open(this);
		open_finalize();
	}

	/******
	 * Reopening and reread the database and refresh the tree.<br/>
	 * If the database has not been opened, it throws an exception.
	 * @throws Exception
	 */
	public void reopen() throws Exception
	{
		if (databaseRepresentation != null)
		{
			databaseRepresentation.open(this);
			open_finalize();
		} else {
			throw new Exception("Database has not been opened.");
		}
	}

	/******
	 * set the database version
	 * 
	 * @param v : version of the database
	 */
	public void setVersion (String version) 
	{
		if (version == null) {
			// very old database
			versionMajor = 1;
			versionMinor = 0;
		}
		
		int ip = version.indexOf('.');
		if (ip>0) {
			versionMajor = Short.parseShort(version.substring(0, ip));
			versionMinor = Short.parseShort(version.substring(ip+1));
		}
	}


	/*************************************************************************
	 *	Returns the name of the experiment.
	 ************************************************************************/	
	public String getName()
	{
		return configuration.getName(ExperimentConfiguration.NAME_EXPERIMENT);
	}

	@Override
	public String toString()
	{
		return getName();
	}

	/*************************************************************************
	 *	Sets the experiment's configuration.
	 *
	 *	This method is to be called only once, during <code>Experiment.open</code>.
	 *
	 ************************************************************************/
	public void setConfiguration(ExperimentConfiguration configuration)
	{
		this.configuration = configuration;
	}

	public ExperimentConfiguration getConfiguration()
	{
		return this.configuration;
	}


	/*************************************************************************
	 *	Returns the default directory from which to resolve relative paths.
	 ************************************************************************/

	public File getDefaultDirectory()
	{
		return getXMLExperimentFile().getParentFile();
	}

	public File getXMLExperimentFile() {
		return databaseRepresentation.getFile();
	}


	/*****
	 * disposing the experiment resources.
	 */
	public void dispose()
	{
		if (rootScope != null) {
			DisposeResourcesVisitor visitor = new DisposeResourcesVisitor();
			rootScope.dfsVisitScopeTree(visitor);
		}
		rootScope = null;
		
		datacentricRootScope   = null;
		databaseRepresentation = null;
	}


	/*************************************************************************
	 * Filter the cct 
	 * <p>caller needs to call postprocess to ensure the callers tree and flat
	 * tree are also filtered </p>
	 * @param filter
	 *************************************************************************/
	public int filter(IFilterData filter)
	{
		// TODO :  we assume the first child is the CCT
		final RootScope rootCCT = (RootScope) rootScope.getChildAt(0);

		// duplicate and filter the cct
		FilterScopeVisitor visitor = new FilterScopeVisitor(rootCCT, filter);
		rootCCT.dfsVisitFilterScopeTree(visitor);

		if (rootCCT.getType() == RootScopeType.CallingContextTree) {
			filter_finalize(rootCCT, filter);
		}
		filterNumScopes = visitor.numberOfFilteredScopes();
		filterStatus	= visitor.getFilterStatus();
		
		return filterNumScopes;
	}

	/****
	 * return the number of matched scopes from the filter.<br/>
	 * Note that this is NOT the number of removed scopes, but the number of
	 * scopes that match the filter pattern.
	 * @return int
	 */
	public int getNumberOfFilteredScopes() {
		return filterNumScopes;
	}
	
	public int getFilterStatus() {
		return filterStatus;
	}
	
	public void setMinMaxCCTID(int min, int max)
	{
		traceAttribute.min_cctid = min;
		traceAttribute.max_cctid = max;
	}
	
	public int getMinCCTID()
	{
		return traceAttribute.min_cctid;
	}
	
	public int getMaxCCTID()
	{
		return traceAttribute.max_cctid;
	}
	
	
	/******
	 * set the trace attributes (if the tracefile exist)
	 * @param attribute
	 */
	public void setTraceAttribute(TraceAttribute attribute) {
		this.traceAttribute = attribute;
	}


	/*****
	 * get the trace attributes. If the database has no traces,
	 * it return null
	 * 
	 * @return trace attributes
	 */
	public BaseTraceAttribute getTraceAttribute() {
		return traceAttribute;
	}


	/************************************************************************
	 * In case the experiment has a CCT, continue to create callers tree and
	 * flat tree for the finalization.
	 * 
	 * @param rootCCT
	 * @param filter
	 ************************************************************************/
	abstract protected void filter_finalize(RootScope rootMain, IFilterData filter);

	abstract protected void open_finalize();

}
 