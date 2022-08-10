package edu.rice.cs.hpcdata.tld;

import java.io.IOException;

import edu.rice.cs.hpcdata.db.version4.MetricValueCollection4;
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
	private ThreadDataCollectionFactory() {
		// no operation
	}
	
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
	public static IThreadDataCollection build(RootScope root) throws IOException
	{
		IThreadDataCollection threadDataCollection = null;

		Experiment experiment = (Experiment) root.getExperiment();
		int version = experiment.getMajorVersion();
		switch(version)
		{
		case 1:
		case Constants.EXPERIMENT_DENSED_VERSION:

			if (experiment.getRawMetrics()!=null) {
				threadDataCollection = new ThreadDataCollection2(experiment);
				String directory = experiment.getDefaultDirectory().getAbsolutePath();
				threadDataCollection.open(root, directory);
			}
			break;
			
		case Constants.EXPERIMENT_SPARSE_VERSION:
			threadDataCollection = new ThreadDataCollection4();
			
			var mvc = (MetricValueCollection4) root.getMetricValueCollection();
			((ThreadDataCollection4)threadDataCollection).init(mvc.getDataSummary());
			
			String directory = experiment.getDefaultDirectory().getAbsolutePath();
			((ThreadDataCollection4)threadDataCollection).open(root, directory);
			break;
		default:
			break;
		}
		
		return threadDataCollection;
	}
}
