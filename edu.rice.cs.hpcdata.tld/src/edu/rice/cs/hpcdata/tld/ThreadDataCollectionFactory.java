package edu.rice.cs.hpcdata.tld;

import java.io.IOException;

import edu.rice.cs.hpcdata.db.version4.MetricValueCollection3;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.tld.v2.ThreadDataCollection2;
import edu.rice.cs.hpcdata.tld.v4.ThreadDataCollection4;
import edu.rice.cs.hpcdata.util.Constants;

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
		case Constants.EXPERIMENT_DENSED_VERSION:

			if (experiment.getRawMetrics()!=null) {
				data_file = new ThreadDataCollection2(experiment);
				String directory = experiment.getDefaultDirectory().getAbsolutePath();
				data_file.open(root, directory);
			}
			break;
			
		case Constants.EXPERIMENT_SPARSE_VERSION:
			data_file = new ThreadDataCollection4();
			
			var mvc = (MetricValueCollection3) experiment.getMetricValueCollection();						
			((ThreadDataCollection4)data_file).init(mvc.getDataSummary());
			
			String directory = experiment.getDefaultDirectory().getAbsolutePath();
			((ThreadDataCollection4)data_file).open(root, directory);
			break;
		default:
			data_file = null;
			break;
		}
		root.getExperiment().setThreadData(data_file);
		
		return data_file;
	}
}
