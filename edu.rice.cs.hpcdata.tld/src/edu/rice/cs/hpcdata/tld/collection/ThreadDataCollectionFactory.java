package edu.rice.cs.hpcdata.tld.collection;

import java.io.IOException;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.util.Constants;

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
	static public IThreadDataCollection build(RootScope root) throws IOException
	{
		IThreadDataCollection data_file = null;

		Experiment experiment = (Experiment) root.getExperiment();
		int version = experiment.getMajorVersion();
		switch(version)
		{
		case 1:
		case 2:
			final BaseMetric []metrics = experiment.getMetricRaw();
			if (metrics!=null) {
				data_file = new ThreadDataCollection2(experiment);
				String directory = experiment.getDefaultDirectory().getAbsolutePath();
				data_file.open(root, directory);
			}
			break;
			
		case Constants.EXPERIMENT_SPARSE_VERSION:
			data_file = new ThreadDataCollection3();
			String directory = experiment.getDefaultDirectory().getAbsolutePath();
			((ThreadDataCollection3)data_file).open(root, directory);
			break;
		default:
			data_file = null;
			break;
		}

		
		return data_file;
	}
}
