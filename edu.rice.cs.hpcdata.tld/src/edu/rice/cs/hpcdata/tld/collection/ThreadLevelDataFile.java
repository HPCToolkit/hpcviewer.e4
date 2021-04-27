package edu.rice.cs.hpcdata.tld.collection;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.rice.cs.hpcdata.experiment.extdata.FileDB2;
import edu.rice.cs.hpcdata.util.Constants;


/*****************************************
 * class to manage data on thread level of a specific experiment
 * 
 *
 *****************************************/
public class ThreadLevelDataFile extends FileDB2 
{

	// header bytes to skip
	static private final int HEADER_LONG	=	32;
	static int recordSz = Constants.SIZEOF_LONG + Constants.SIZEOF_LONG;

	private ExecutorService threadExecutor;
	private int num_threads;
	
	public ThreadLevelDataFile() {
	}
	
	
	/***
	 * Open a metric database (metric-db) file
	 * @param filename
	 * @throws IOException
	 */
	public void open(String filename) throws IOException
	{
		super.open(filename, HEADER_LONG, recordSz);
		final int numWork = getNumberOfRanks();
		num_threads = Math.min(numWork, Runtime.getRuntime().availableProcessors());
		threadExecutor = Executors.newCachedThreadPool(); 
	}
	
	
	/**
	 * return all metric values of a specified node and metric index
	 * 
	 * @param nodeIndex: normalized node index 
	 * @param metricIndex: the index of the metrics
	 * @param numMetrics: the number of metrics in the experiment
	 * @return
	 */
	public double[] getMetrics(long nodeIndex, int metricIndex, int numMetrics) 
	{
	
		final double []metrics = new double[getNumberOfRanks()];

		ExecutorCompletionService<Integer> ecs = new ExecutorCompletionService<Integer>(threadExecutor);

		final int numWork = getNumberOfRanks();
		final int numWorkPerThreads = (int) Math.ceil((float)numWork / (float)num_threads);
		
		// --------------------------------------------------------------
		// assign each thread for a range of files to gather the data
		// --------------------------------------------------------------
		for (int i=0; i<num_threads; i++) {
			
			final int start = i * numWorkPerThreads;
			final int end = Math.min(start+numWorkPerThreads, numWork);
			
			DataReadThread thread = new DataReadThread(nodeIndex, metricIndex, numMetrics, start, end,
					this, metrics);
			ecs.submit(thread);
		}
		
		// --------------------------------------------------------------
		// wait until all threads finish
		// --------------------------------------------------------------
		for (int i=0; i<num_threads; i++) {
			try {
				ecs.take().get();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return metrics;
	}

	/***
	 * get a specific metric value for a certain node, metric and profile id.
	 * 
	 * @param nodeIndex
	 * @param metricIndex
	 * @param profileId
	 * @param numMetrics
	 * @return
	 * @throws IOException
	 */
	public double getMetric(long nodeIndex, int metricIndex, int profileId, int numMetrics) throws IOException
	{
		long offset = getOffsets()[profileId];
		long position = getFilePosition(nodeIndex, metricIndex, numMetrics);
		
		return getDouble(offset + position);
	}

	public double[] getScopeMetrics(int thread_index, int metricIndex, int num_metrics) throws IOException
	{
		long offset = getOffsets()[thread_index];
		int num_cct = getNumberOfCCT(thread_index, num_metrics);
		double []values = new double[num_cct];
		for (int i=0; i<num_cct; i++)
		{
			long position = getFilePosition(i+1, metricIndex, num_metrics);
			values[i] = getDouble(offset + position);
		}
		return values;
	}
	
	@Override
	public void dispose()
	{
		threadExecutor.shutdown();
		super.dispose();
	}
	
	private int getNumberOfCCT(int thread_id, int num_metrics)
	{
		long []offsets = getOffsets();
		long offset1 = offsets[thread_id];
		long offset2 = thread_id == offsets.length-1 ? offsets[thread_id-1] : offsets[thread_id + 1];
		
		long distance = Math.abs(offset2-offset1);
		int num_cct = (int) (distance / (num_metrics * Constants.SIZEOF_LONG));
		
		return num_cct;
	}
	
	/**
	 * get a position for a specific node index and metric index
	 * @param nodeIndex
	 * @param metricIndex
	 * @param num_metrics
	 * @return
	 */
	static private long getFilePosition(long nodeIndex, int metricIndex, int num_metrics) {
		return ((nodeIndex-1) * num_metrics * Constants.SIZEOF_LONG) + (metricIndex * Constants.SIZEOF_LONG) +
			// header to skip
			HEADER_LONG;
	}
	
	
	/***
	 * Thread helper class to read a range of files
	 *
	 */
	static private class DataReadThread implements Callable<Integer> 
	{
		final private long _nodeIndex;
		final private int _metricIndex;
		final private int _numMetrics;
		final private int _indexFileStart, _indexFileEnd;
		final private double _metrics[];
		final private ThreadLevelDataFile data;
		
		/***
		 * Initialization for reading a range of file from indexFileStart to indexFileEnd
		 * The caller has to create a thread and collect the output from metrics[] variable
		 * 
		 * Note: the output metrics has to have the same range as indexFileStart ... indexFileEnd
		 * 
		 * @param nodeIndex:	cct node index
		 * @param metricIndex:	metric index
		 * @param numMetrics:	number of metrics
		 * @param indexFileStart:	the beginning of file index
		 * @param indexFileEnd:		the end of file index
		 * @param monitor:		monitor for long process
		 * @param metrics:		output to gather metrics
		 */
		public DataReadThread(long nodeIndex, int metricIndex, int numMetrics,
				int indexFileStart, int indexFileEnd, ThreadLevelDataFile data,
				double metrics[]) {
			_nodeIndex = nodeIndex;
			_metricIndex = metricIndex;
			_numMetrics = numMetrics;
			_indexFileStart = indexFileStart;
			_indexFileEnd = indexFileEnd;
			_metrics = metrics;
			this.data = data;
		}
		

		@Override
		public Integer call() throws Exception {
			final long pos_relative = getFilePosition(_nodeIndex, _metricIndex, _numMetrics);
			final long offsets[] = data.getOffsets();
			
			for (int i=_indexFileStart; i<_indexFileEnd; i++) {
				final long pos_absolute = offsets[i] + pos_relative;
				try {
					_metrics[i] = data.getDouble(pos_absolute);
				} catch (Exception e) {
					System.err.println( e.getClass() + ": " + e.getMessage() + "\n" + 
										"Error reading at position: " + pos_absolute);
					break;
				}
			}
			return Integer.valueOf(_indexFileEnd);
		}
	}
}
