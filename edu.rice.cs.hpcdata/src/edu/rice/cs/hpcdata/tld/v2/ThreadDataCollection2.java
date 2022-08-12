package edu.rice.cs.hpcdata.tld.v2;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.extdata.AbstractThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.metric.MetricRaw;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.util.IProgressReport;
import edu.rice.cs.hpcdata.util.MergeDataFiles;

/******************************************************************
 * 
 * Class to manage a collection of thread data for database version 1 and 2.
 * Version 1 is when the database comprises of multiple files per thread,
 * while version 2 is when the files are merged into one mega file.
 *
 ******************************************************************/
public class ThreadDataCollection2 extends AbstractThreadDataCollection
{
	private ThreadLevelDataFile[] dataFile;
	private File directory;
	private Experiment experiment;

	public ThreadDataCollection2(Experiment experiment)
	{
		this.experiment = experiment;
		int numMetrics  = experiment.getRawMetrics().size();
		dataFile		= new ThreadLevelDataFile[numMetrics];
	}
	
	@Override
	public void open(RootScope root, String directory) throws IOException {
		File dir = new File(directory);
		if (dir.isFile())
			this.directory = dir.getParentFile();
		else
			this.directory = dir;
	}
	

	@Override
	public double getMetric(long nodeIndex, int metricIndex, IdTuple idtuple, int numMetrics) 
			throws IOException {
		// check if the data already exists or not
		ensureDataFile(metricIndex);
		
		return dataFile[metricIndex].getMetric(nodeIndex, metricIndex, idtuple, numMetrics);
	}

	
	@Override
	public double[] getMetrics(long nodeIndex, int metricIndex, int numMetrics) 
			throws Exception {
		// check if the data already exists or not
		ensureDataFile(metricIndex);
		return dataFile[metricIndex].getMetrics(nodeIndex, metricIndex, numMetrics);
	}
	
	@Override
	public double[] getScopeMetrics(int threadId, int metricIndex, int numMetrics) throws IOException
	{
		// check if the data already exists or not
		ensureDataFile(metricIndex);
		return dataFile[metricIndex].getScopeMetrics(threadId, metricIndex, numMetrics);
	}


	@Override
	public boolean isAvailable() {
		return dataFile != null && dataFile.length>0;
	}

	
	@Override
	public double[] getRankLabels() throws IOException {
		ensureDataFile(0);
		final String []labels = dataFile[0].getRankLabels();
		
		double []rankLabels = new double[labels.length];
		
		// try to reorder the rank so that the threads can be evenly sparsed
		// for example if the ranks are 0.0, 0.1, 0.2, 1.0, 1.1, 1.2
		// we want to convert it into:  0.0, 0.3, 0.6, 1.0, 1.3, 1.6
		for(int i=0; i<labels.length; i++)
		{
			double rankInDouble = Double.parseDouble(labels[i]); 
			rankLabels[i] = rankInDouble;
		}
		
		return rankLabels;
	}


	@Override
	public String[] getRankStringLabels() throws IOException {
		ensureDataFile(0);
		return dataFile[0].getRankLabels();
	}

	@Override
	public int getParallelismLevel() throws IOException {
		ensureDataFile(0);
		return dataFile[0].getParallelismLevel();
	}

	@Override
	public String getRankTitle() throws IOException {
		String title;
		if (getParallelismLevel() > 1)
		{
			title = "Process.Thread";
		} else {
			if (dataFile[0].isMultiProcess())
				title = "Process";
			else 
				title = "Thread";
		}
		return title;
	}

	@Override
	public void dispose() {
		if (dataFile != null)
		{
			for(ThreadLevelDataFile df : dataFile)
			{
				if (df != null)
					df.dispose();
			}
		}
	}


	private void ensureDataFile(int metricIndex) throws IOException
	{
		if (dataFile[metricIndex] != null)
		{
			return;
		}
		// data hasn't been created. Try to merge and open the file
		String file = ThreadLevelDataCompatibility.getMergedFile(experiment, directory, metricIndex);
		if (file != null)
		{
			dataFile[metricIndex] = new ThreadLevelDataFile();
			dataFile[metricIndex].open(file);
		} else {
			throw new IOException("No thread-level data in " + directory.getAbsolutePath());
		}
	}
	
	/**
	 * class to cache the name of merged thread-level data files. 
	 * We will ask A LOT the name of merged files, thus keeping in cache will avoid us to check to often
	 * if the merged file already exist or not
	 * 
	 * The class also check compatibility with the old version.
	 *
	 */
	private static class ThreadLevelDataCompatibility 
	{
		/**
		 * method to find the name of file for a given metric ID. 
		 * If the files are not merged, it will be merged automatically
		 * 
		 * The name of the merge file will depend on the glob pattern
		 * 
		 * @param directory
		 * @param metricRawId
		 * @return
		 * @throws IOException
		 */
		public static String getMergedFile(Experiment experiment, File directory, int metricRawId) throws IOException 
		{
			final HashMap<String, String> listOfFiles = new HashMap<>();
			final MetricRaw metric = (MetricRaw) experiment.getRawMetrics().get(metricRawId);
			final String globInputFile = metric.getGlob();
			
			// assuming the number of merged experiments is less than 10
			final char experiment_char = globInputFile.charAt(0);
			int experiment_id = 1;
			
			if (experiment_char>='0' && experiment_char<='9') {
				experiment_id = experiment_char - '0';
			}
			
			// ------------------------------------------------------------------------------------
			// given the metric raw id, reconstruct the name of raw metric data file
			// for instance, if raw metric id = 1, then the file should be experiment-1.mdb
			// ------------------------------------------------------------------------------------
			final String outputFile = directory.getAbsolutePath() + File.separatorChar + 
					"experiment-" + experiment_id + ".mdb";

			// check if the file is already merged
			String cacheFileName = listOfFiles.get(outputFile);
			
			if (cacheFileName == null) {
				
				// ----------------------------------------------------------
				// the file doesn't exist, we need to merge metric-db files
				// ----------------------------------------------------------
				// check with the old version of thread level data
				checkOldVersionOfData(directory);
				
				final ProgressReport progress= new ProgressReport(  );
				
				// ------------------------------------------------------------------------------------
				// the compact method will return the name of the compacted files.
				// if the file doesn't exist, it will be created automatically
				// ------------------------------------------------------------------------------------
				MergeDataFiles.MergeDataAttribute att = MergeDataFiles.merge(directory, 
						globInputFile, outputFile, progress);
				
				if (att == MergeDataFiles.MergeDataAttribute.FAIL_NO_DATA) {
					// ------------------------------------------------------------------------------------
					// the data doesn't exist. Let's try to use experiment.mdb for compatibility with the old version
					// ------------------------------------------------------------------------------------
					cacheFileName =  directory.getAbsolutePath() + File.separatorChar + "experiment.mdb";
					att = MergeDataFiles.merge(directory, globInputFile, cacheFileName, progress);
					
					if (att == MergeDataFiles.MergeDataAttribute.FAIL_NO_DATA)
						return null;
				} else {
					cacheFileName = outputFile;
				}
				listOfFiles.put(outputFile, cacheFileName);

			}
			return cacheFileName;
		}
		
		private static void checkOldVersionOfData(File directory) {
			
			String oldFile = directory.getAbsolutePath() + File.separatorChar + "experiment.mdb"; 
			File file = new File(oldFile);
			
			if (file.canRead()) {
				// old file already exist, needs to warn the user
			}
		}
	}

	
	
	/*******************
	 * Temporary Quick fix: Empty Progress bar
	 *
	 */
	private static class ProgressReport implements IProgressReport 
	{

		public ProgressReport()
		{
			// no action needed
		}
		
		public void begin(String title, int numTasks) {
			// no action needed
		}

		public void advance() {
			// no action needed
		}

		public void end() {
			// no action needed
		}
	}



	@Override
	public List<IdTuple> getIdTuples() {
		if (dataFile == null || dataFile.length == 0)
			return Collections.emptyList();
		
		return dataFile[0].getIdTuple(IdTupleOption.BRIEF);
	}
}
