package edu.rice.cs.hpcviewer.ui.metric;

import java.io.IOException;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;

/*************************************************
 * 
 * Class factory to generate an {@link IThreadDataCollection} 
 * object based on the version of the experiment database
 *
 *************************************************/
public final class ThreadDataCollectionFactory 
{
	/*******
	 * Build an {@link IThreadDataCollection} object
	 *  
	 * @param experiment : experiment database. Should have thread-level data
	 * 
	 * @return IThreadDataCollection : interface to access thread-level data,
	 * or null if the database has no thread-level data.
	 * 
	 * @throws IOException
	 *******/
	static public IThreadDataCollection build(Experiment experiment) throws IOException
	{
		final BaseMetric []metrics = experiment.getMetricRaw();
		IThreadDataCollection data_file = null;
		if (metrics!=null) {
			int version = experiment.getMajorVersion();
			String directory = experiment.getDefaultDirectory().getAbsolutePath();
			switch(version)
			{
			case 1:
			case 2:
				data_file = new ThreadDataCollection2(experiment);
				data_file.open(directory);
				break;
			case 3:
				data_file = new ThreadDataCollection3();
				data_file.open(directory);
				break;
			default:
				data_file = null;
				break;
			}
		}
		return data_file;
	}
	
	/***************
	 * Check if the thread-level data is available.
	 * 
	 * @param experiment : experiment database
	 * @return true if the data exists, false otherwise
	 ***************/
	static public boolean isThreadDataAvailable(Experiment experiment) 
	{
		final BaseMetric []metrics = experiment.getMetricRaw();
		return (metrics != null); 
	}
}
