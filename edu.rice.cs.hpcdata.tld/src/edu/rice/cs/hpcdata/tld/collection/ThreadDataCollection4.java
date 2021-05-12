package edu.rice.cs.hpcdata.tld.collection;


import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.version4.DataSummary;
import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.BaseExperiment.Db_File_Type;
import edu.rice.cs.hpcdata.experiment.extdata.AbstractThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.tld.plot.DataPlot;
import edu.rice.cs.hpcdata.tld.plot.DataPlotEntry;

/*******************************************************************
 * 
 * Class to manage a collection of metric plot data for database v.3
 *
 *******************************************************************/
public class ThreadDataCollection4 extends AbstractThreadDataCollection
{
	private DataPlot    data_plot;
	private DataSummary data_summary;
	
	@Override
	public void open(RootScope root, String directory) throws IOException {
		data_plot = new DataPlot();
		data_plot.open(directory + File.separatorChar + 
				BaseExperiment.getDefaultDatabaseName(Db_File_Type.DB_PLOT));
		
		data_summary = root.getDataSummary();
	}

	@Override
	public boolean isAvailable() {
		return ((data_plot != null));
	}

	@Override
	public double[] getRankLabels() {		
		return data_summary.getDoubleLableIdTuples();
	}


	@Override
	public String[] getRankStringLabels() throws IOException {
		return data_summary.getStringLabelIdTuples();
	}

	@Override
	public int getParallelismLevel() {
		return data_summary.getMaxLevels();
	}

	@Override
	public String getRankTitle() {
		return "Rank";
	}

	@Override
	public double getMetric(long nodeIndex, int metricIndex, int profileId, int numMetrics) throws IOException {

		if (data_summary == null)
			return 0.0d;

		return data_summary.getMetric(profileId, (int) nodeIndex, metricIndex);
	}

	@Override
	public double[] getMetrics(long nodeIndex, int metricIndex, int numMetrics)
			throws IOException 
			{
		final DataPlotEntry []entry = data_plot.getPlotEntry((int) nodeIndex, metricIndex);
		
		List<IdTuple> list = data_summary.getIdTuple();
		
		final int num_ranks 	= Math.max(1, list.size());
		final double []metrics	= new double[num_ranks];
		
		// if there is no plot data in the database, we return an array of zeros
		// (assuming Java will initialize metrics[] with zeros)
		if (entry != null)
		{	
			for(DataPlotEntry e : entry)
			{
				int profile = data_summary.getProfileIndexFromOrderIndex(e.tid);
				
				// minus 1 because the index is based on profile number.
				// unfortunately, the profile number starts with number 1 instead of 0
				// the profile 0 is reserved for summary profile. sigh
				metrics[profile] = e.metval;
			}
		}
		return metrics;
	}

	@Override
	public void dispose() {
		data_plot.dispose();
	}

	@Override
	public double[] getScopeMetrics(int thread_id, int MetricIndex,
			int numMetrics) throws IOException {

		// TODO: not implemented
		return null;
	}
}
