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
	
	/** version of the database **/
	private short versionMajor;
	private short versionMinor;

	protected IDatabaseRepresentation databaseRepresentation;
	
	private IdTupleType idTupleType;
	private IThreadDataCollection threadData;
	
	private BaseTraceAttribute traceAttribute = new TraceAttribute();

	
	/***
	 * the root scope of the experiment
	 * 
	 * @param the root scope
	 */
	public void setRootScope(Scope newRootScope) {
		this.rootScope   = (RootScope) newRootScope;
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
		if (threadData != null) {
			// we don't need to dispose the resources if only resetting
			// or filtering the database.
			// This is to avoid null pointer exception since in meta.db,
			// disposing threadData means closing access to plot.db file
			//
			// threadData.dispose();
		}
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
	@Override
	public ICallPath getScopeMap() {
		return traceAttribute.mapCpidToCallpath;
	}


	/**
	 * @param scopeMap the scopeMap to set
	 */
	@Override
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
		if (databaseRepresentation == null)
			throw new IOException("Database has not been opened.");
		
		databaseRepresentation.open(this);
		open_finalize();
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
	 *
	 * @return File
	 ************************************************************************/

	public File getDefaultDirectory()
	{
		return getExperimentFile().getParentFile();
	}

	
	/****
	 * Retrieve the file reference of the main database file.
	 * For the old version it's the experiment.xml, for the newer version 
	 * it's the meta.db file.
	 * 
	 * @return File
	 */
	public File getExperimentFile() {
		return databaseRepresentation.getFile();
	}

	
	@Override
	public int getTraceDataVersion() {
		return databaseRepresentation.getTraceDataVersion();
	}

	/*****
	 * disposing the experiment resources.
	 */
	@Override
	public void dispose()
	{
		if (rootScope != null) {
			rootScope.disposeSelfAndChildren();
			rootScope = null;
		}
		
		if (threadData != null) {
			threadData.dispose();
			threadData = null;
		}
		if (traceAttribute != null) {
			traceAttribute.dispose();
			traceAttribute = null;
		}
		
		databaseRepresentation = null;
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

	protected abstract void open_finalize();
}
 