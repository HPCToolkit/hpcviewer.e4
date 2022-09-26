package edu.rice.cs.hpcdata.experiment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.visitors.DisposeResourcesVisitor;
import edu.rice.cs.hpcdata.experiment.scope.visitors.FilterScopeVisitor;
import edu.rice.cs.hpcdata.filter.IFilterData;
import edu.rice.cs.hpcdata.tld.ThreadDataCollectionFactory;
import edu.rice.cs.hpcdata.trace.BaseTraceAttribute;
import edu.rice.cs.hpcdata.trace.TraceAttribute;
import edu.rice.cs.hpcdata.util.ICallPath;
import edu.rice.cs.hpcdata.util.IUserData;


/***
 * Base abstract experiment that handle only call path.
 * 
 * No metric is associated in this experiment
 *
 */
public abstract class BaseExperiment implements IExperiment 
{
	/** The experiment's configuration. */
	protected ExperimentConfiguration configuration;

	protected RootScope rootScope;
	
	protected RootScope datacentricRootScope;
	
	/** version of the database **/
	private short versionMajor;
	private short versionMinor;

	protected IDatabaseRepresentation databaseRepresentation;
	
	private int filterNumScopes = 0;
	private int filterStatus;
	private IdTupleType idTupleType;
	private IThreadDataCollection threadData;
	
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

	public String getDirectory() {
		var location = databaseRepresentation.getFile();
		if (location.isDirectory())
			return location.getAbsolutePath();
		return location.getParentFile().getAbsolutePath();
	}

	/***
	 * set the new id tuple type
	 * @param idTupleType
	 */
	@Override
	public void setIdTupleType(IdTupleType idTupleType) {
		this.idTupleType = idTupleType;
	}
	
	
	/****
	 * get this database's id tuple type
	 * @return {@code IdTupleType}
	 */
	@Override
	public IdTupleType getIdTupleType() {
		if (idTupleType == null) {
			idTupleType = IdTupleType.createTypeWithOldFormat();
		}
			
		return idTupleType;
	}
	

	/***
	 * Return the IThreadDataCollection of this root if exists.
	 * 
	 * @return
	 * @throws IOException 
	 */
	@Override
	public IThreadDataCollection getThreadData() throws IOException {
		if (threadData == null) {
			var root = getRootScope(RootScopeType.CallingContextTree);
			threadData = ThreadDataCollectionFactory.build(root);
		}
		return threadData;
	}
	

	/****
	 * Reset the thread data. This is important to reset the data
	 * once the database has been changed (like has been filtered).
	 * 
	 */
	public void resetThreadData() {
		threadData = null;
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
	@Override
	public int getMaxDepth() {
		return traceAttribute.maxDepth;
	}


	/**
	 * Set the new depth maximum in the CCT
	 * @param maxDepth 
	 * 		the maxDepth to set
	 */
	@Override
	public void setMaxDepth(int maxDepth) {
		traceAttribute.maxDepth = maxDepth;
	}


	/**
	 * @return the scopeMap
	 */
	public ICallPath getScopeMap() {
		return traceAttribute.mapCpidToCallpath;
	}


	/**
	 * @param scopeMap the scopeMap to set
	 */
	public void setScopeMap(ICallPath scopeMap) {
		traceAttribute.mapCpidToCallpath = scopeMap;
	}


	public List<Scope> getRootScopeChildren() {
		RootScope root = (RootScope) getRootScope();

		if (root != null)
			return root.getChildren();
		else
			return Collections.emptyList();
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
		if (root == null)
			return null;
		
		for (int i=0; i<root.getSubscopeCount(); i++)
		{
			if (((RootScope)root.getSubscope(i)).getType() == type)
				return (RootScope) root.getSubscope(i);
		}
		return null;
	}
	
	/****
	 * open a local database
	 * 
	 * @param fileExperiment : file of the experiment xml
	 * @param userData : map of user preferences
	 * @param needMetric : whether we need to assign metrics or not
	 * @throws Exception
	 */
	public void open(File fileExperiment, 
					 IUserData<String, String> userData, 
					 boolean needMetric)
			throws	Exception
	{
		databaseRepresentation = new LocalDatabaseRepresentation(fileExperiment, userData, needMetric);
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
	public void open(InputStream expStream, 
					 IUserData<String, String> userData,
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
			throw new IOException("Database has not been opened.");
		}
	}

	
	/****
	 * Update the database representation of this experiment database.
	 * A database representation can be remote or local.
	 * 
	 * @param databaseRepresentation
	 */
	public void setDatabaseRepresentation(IDatabaseRepresentation databaseRepresentation) {
		this.databaseRepresentation = databaseRepresentation;
	}
	
	/******
	 * set the database version
	 * 
	 * @param version
	 * 			version of the database in format {@code Major.Minor}
	 */
	@Override
	public void setVersion (String version) 
	{
		if (version == null) {
			// very old database
			version = "1.0";
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
	@Override
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
	@Override
	public void setConfiguration(ExperimentConfiguration configuration)
	{
		this.configuration = configuration;
	}

	@Override
	public ExperimentConfiguration getConfiguration()
	{
		return this.configuration;
	}


	/*************************************************************************
	 *	Returns the default directory from which to resolve relative paths.
	 ************************************************************************/

	public File getDefaultDirectory()
	{
		return getExperimentFile().getParentFile();
	}

	public File getExperimentFile() {
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
	 * <p>caller needs to call post-process to ensure the callers tree and flat
	 * tree are also filtered </p>
	 * @param filter
	 *************************************************************************/
	@Override
	public int filter(IFilterData filter)
	{
		if (rootScope == null)
			// case of corrupt database
			return 0;
		
		// TODO :  we assume the first child is the CCT
		final RootScope rootCCT = getRootScope(RootScopeType.CallingContextTree);

		// duplicate and filter the cct
		FilterScopeVisitor visitor = new FilterScopeVisitor(rootCCT, filter);
		rootCCT.dfsVisitFilterScopeTree(visitor);

		// finalize the filter: for hpcviewer, we need to prepare to create subtrees:
		// bottom-up, flat and optionally data-centric tree
		
		if (rootCCT.getType() == RootScopeType.CallingContextTree) {
			filter_finalize(rootCCT, filter);
		}
		filterNumScopes = visitor.numberOfFilteredScopes();
		filterStatus	= visitor.getFilterStatus();
		setMaxDepth(visitor.getMaxDepth());
		
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
	public void setTraceAttribute(BaseTraceAttribute attribute) {
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
 