package edu.rice.cs.hpcdata.experiment.extdata;

import java.io.IOException;
import java.util.List;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

/********************************************************************************
 * 
 * Interface to collect data needed for plot graph and thread view
 *
 ********************************************************************************/
public interface IThreadDataCollection 
{
	/****
	 * Open a directory which may contain data for plot graph.
	 * If the directory has no plot data, it throws an exception.
	 *  
	 * @param directory : the database directory
	 * 
	 * @throws IOException
	 */
	public void 	open(RootScope root, String directory) throws IOException;
	
	/****
	 * Check if the opened directory has a plot data or not
	 * @return true if plot data exist, false otherwise
	 */
	public boolean 	isAvailable();
	
	
	public List<IdTuple> getIdTuples();
	
	/*****
	 * Get a list of the labels for ranks (x-axis)
	 * @return
	 * @throws IOException 
	 */
	public double[]	getRankLabels() throws IOException;	

	
	public String[]	getRankStringLabels() throws IOException;	

	/*****
	 * Similar to {@link getRankLabels()}, but the labels are evenly sparsed
	 * which is ideal for a plot graph.
	 * 
	 * It recomputes the values of x into an evenly spread values if the application is a 
	 * hybrid parallel code.<br/><p>
	 * For instance, if the database has the following threads: 0.0, 0.1, 0.2, 2.0, and 2.1
	 * this method will return an "evenly spread" values into:  0.0, 0.3, 0.6, 2.0, and 2.5
	 * </p>
	 * This method is only used for plotting the graph. 

	 * @return
	 * @throws IOException
	 */
	public double[] getEvenlySparseRankLabels() throws IOException;
	
	/****
	 * Get the level of parallelism. If it's a thread only or process only
	 * application, it returns 1, if it's a hybrid it returns 2. 
	 * (more than 2 is not supported at the moment).
	 * 
	 * @return
	 * @throws IOException 
	 */
	public int 		getParallelismLevel() throws IOException;
	
	/****
	 * Get the title of the rank (process, threads, ...)
	 * @return
	 * @throws IOException 
	 */
	public String   getRankTitle() throws IOException;
	
	
	/****
	 * A generic version of getMetric for a given Scope and BaseMetric.
	 * 
	 * @see getMetric(long nodeIndex, int metricIndex, IdTuple idtuple, int numMetrics)
	 * 
	 * @param scope
	 * @param metric
	 * @param idtuple
	 * @param numMetrics
	 * 
	 * @throws IOException
	 *  
	 * @return
	 */
	public double   getMetric(Scope scope, BaseMetric metric, IdTuple idtuple, int numMetrics)
			throws IOException ;
	
	
	/****
	 * A generic version of {@code getMetric} for a given {@code Scope} and {@code BaseMetric}
	 * 
	 * @param scope
	 * @param metric
	 * @param numMetrics
	 * @return
	 * 
	 * @throws Exception
	 */
	public double[] getMetrics(Scope scope, BaseMetric metric, int numMetrics)
			throws  Exception;

	
	/****
	 * Get a metric value for a specific node, with a specific metric and 
	 * a specific profile (or thread) id.
	 * 
	 * @param nodeIndex 
	 * 			The cct index
	 * @param metricIndex 
	 * 			The index or id of the metric
	 * @param idtuple
	 * 			The id-tuple (profile information)
	 * @param numMetrics 
	 * 			The number of the metrics
	 * 
	 * @return double
	 */
	public double   getMetric(long nodeIndex, int metricIndex, IdTuple idtuple, int numMetrics)
			throws IOException ;
	
	/*****
	 * Get an array of metrics of a specified node and metric.
	 * It returns the array of metric values indexed from its thread or profile or rank number.
	 * This method is useful to plot metric values.
	 * 
	 * @param nodeIndex
	 * @param metricIndex
	 * @param numMetrics
	 * 
	 * @return double[]
	 * @throws Exception 
	 */
	public double[] getMetrics(long nodeIndex, int metricIndex, int numMetrics) 
			throws  Exception;

	/****
	 * get the array of metric values for all CCT of a specified metric and
	 * thread ID.
	 * This method is useful to list ccts and its metric value.
	 * 
	 * @param thread_id : thread ID
	 * @param MetricIndex : metric index
	 * @param numMetrics : num metrics
	 * 
	 * @return array of metric values for each CCT 
	 */
	public double[] getScopeMetrics(int thread_id, int MetricIndex, int numMetrics)
			 throws IOException;
	
	/****
	 * Method to be called at the end of the execution, needed to dispose
	 * the allocated resources.
	 */
	public void		dispose();

	
	/****
	 * Convenient method to get the list of id tuples excluding the gpus.
	 * This is mostly used in plot graph as we don't plot gpu streams at the moment.
	 * 
	 * @param idtype
	 * @return
	 */
	public List<IdTuple> getIdTupleListWithoutGPU(IdTupleType idtype);

	
	/****
	 * Convenient method to get the list id tuple labels without gpus.
	 * This is mostly used in plot graph as we don't plot gpu streams at the moment.
	 * 
	 * @param idtype
	 * @return
	 */
	Object[] getIdTupleLabelWithoutGPU(IdTupleType idtype);
}
