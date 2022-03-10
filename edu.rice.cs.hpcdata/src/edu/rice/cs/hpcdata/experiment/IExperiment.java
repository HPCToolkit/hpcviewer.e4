package edu.rice.cs.hpcdata.experiment;

import java.util.List;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricValueCollection;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.filter.IFilterData;
import edu.rice.cs.hpcdata.trace.BaseTraceAttribute;



/**********************************
 * 
 * Main interface for a database (a.k.a experiment).
 * A database can be local, remote, dense, sparse or binary.
 * It's all depend on the implementation how to represent the database.
 *
 **********************************/
public interface IExperiment {

	/****
	 * Retrieve the name or title of the database
	 * @return
	 */
	public String getName();
	
	/****
	 * Set the main root for this database. 
	 * The root usually contains three sub-roots: top-down, bottom-up and flat roots.
	 * 
	 * @param rootScope
	 * 			The main root.
	 */
	public void setRootScope(Scope rootScope);
	
	/*****
	 * Retrieve the main root of this database
	 * @return
	 */
	public Scope getRootScope();
	

	/****
	 * Set a thread data object
	 * 
	 * @param data_file
	 */
	public void setThreadData(IThreadDataCollection data_file);

	/***
	 * Return the IThreadDataCollection of this root if exists.
	 * 
	 * @return
	 */
	public IThreadDataCollection getThreadData();
	
	/****
	 * Set the default metric value collection object. 
	 * This object is used to generate IMetricValueCollection instance inside a scope.
	 * 
	 * For sparse and dense databases have different implementation of IMetricValueCollection.
	 * 
	 * @param mvc
	 */
	public void setMetricValueCollection(IMetricValueCollection mvc);
	
	
	/****
	 * Get the object of IMetricValueCollection.
	 * If it return null, the caller has to create its own IMetricValueCollection.
	 * 
	 * @return
	 */
	public IMetricValueCollection getMetricValueCollection();
	
	/*******
	 * Retrieve the sub-roots of this database
	 * @return
	 * 		{@code List} of sub-roots
	 */
	public List<?> getRootScopeChildren();
	
	/****
	 * Clone this database.
	 * Unlike Java's {@code clone} method, this method is public thus can be called by anyone
	 * @return
	 */
	public IExperiment duplicate();


	/******
	 * set the database version
	 * 
	 * @param version
	 * 			version of the database in format {@code Major.Minor}
	 */
	void setVersion(String version);

	/****
	 * Retrieve the major version of @Override
	the database.
	 * @return int
	 */
	public int getMajorVersion();

	
	/****
	 * Get the absolute path of the database
	 * 
	 * @return
	 */
	public String getPath();

	
	/***
	 * Filter the current cct with a given filter set
	 * @param filter
	 * 			a filter set 
	 * @return
	 * 			the number of filtered nodes
	 */
	public int filter(IFilterData filter);

	
	/***
	 * If exist, retrieve the trace attribute.
	 * 
	 * @return {@code BaseTraceAttribute}
	 * 			This can be null if the database has no trace data 
	 * 		
	 */
	public BaseTraceAttribute getTraceAttribute();

	/***
	 * set the new id tuple type
	 * 
	 * @param idTupleType
	 */
	public void setIdTupleType(IdTupleType idTupleType);
	
	/***
	 * get the id tuple type
	 * @return
	 */
	public IdTupleType getIdTupleType();

	/****
	 * Get the list of metrics
	 * @return a list of metrics
	 */
	public List<BaseMetric> getMetrics();

	/***
	 * Sets the experiment's configuration.
	 * This method is to be called only once, during <code>Experiment.open</code>.
	 * 
	 * @param configuration
	 * 			the new configuration
	 */
	public void setConfiguration(ExperimentConfiguration configuration);

	/****
	 * Get the database configuration
	 * 
	 * @return
	 */
	public ExperimentConfiguration getConfiguration();

	
	/**
	 * Set the new depth maximum in the CCT
	 * @param maxDepth 
	 * 		the maxDepth to set
	 */
	public void setMaxDepth(int maxDepth);
	
	/**
	 * @return the maxDepth
	 */
	public int getMaxDepth();
}
