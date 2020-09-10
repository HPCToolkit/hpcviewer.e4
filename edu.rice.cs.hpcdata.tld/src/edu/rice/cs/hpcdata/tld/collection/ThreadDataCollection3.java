package edu.rice.cs.hpcdata.tld.collection;


import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.rice.cs.hpc.data.db.DataSummary;
import edu.rice.cs.hpc.data.db.IdTuple;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.BaseExperiment.Db_File_Type;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.AbstractThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.tld.plot.DataPlot;
import edu.rice.cs.hpcdata.tld.plot.DataPlotEntry;

/*******************************************************************
 * 
 * Class to manage a collection of metric plot data for database v.3
 *
 *******************************************************************/
public class ThreadDataCollection3 extends AbstractThreadDataCollection
{
	private DataPlot    data_plot;
	private DataSummary data_summary;
	private BaseExperiment experiment;
	
	@Override
	public void open(RootScope root, String directory) throws IOException {
		data_plot = new DataPlot();
		data_plot.open(directory + File.separatorChar + 
				BaseExperiment.getDefaultDatabaseName(Db_File_Type.DB_PLOT));
		data_summary = root.getDataSummary();
		
		experiment = root.getExperiment();
	}

	@Override
	public boolean isAvailable() {
		return ((data_plot != null));
	}

	@Override
	public double[] getRankLabels() {
		List<IdTuple> list = data_summary.getTuple();
		
		double []labels = new double[list.size()-1];
		
		for(int i=0; i<labels.length; i++) {
			
			// we skip the first index because it's summary profile
			// we don't want to include that in the list of ranks
			
			IdTuple tuple = list.get(i+1);
			labels[i] = tuple.toLabel();
		}
		
		return labels;
	}


	@Override
	public String[] getRankStringLabels() throws IOException {
		List<IdTuple> list = data_summary.getTuple();
		
		String []labels = new String[list.size()-1];
		
		for(int i=0; i<labels.length; i++) {
			
			// we skip the first index because it's summary profile
			// we don't want to include that in the list of ranks
			
			IdTuple tuple = list.get(i+1);
			labels[i] = tuple.toString();
		}
		
		return labels;
	}

	@Override
	public int getParallelismLevel() {
		List<IdTuple> list = data_summary.getTuple();
		IdTuple tuple = list.get(1);
		
		return tuple.length;
	}

	@Override
	public String getRankTitle() {
		return "Rank";
	}

	@Override
	public double[] getMetrics(long nodeIndex, int metricIndex, int numMetrics)
			throws IOException 
			{
		final DataPlotEntry []entry = data_plot.getPlotEntry((int) nodeIndex, metricIndex);
		
		List<IdTuple> list = data_summary.getTuple();
		
		final int num_ranks 		= list.size()-1;
		final double []metrics		= new double[num_ranks];
		
		// if there is no plot data in the database, we return an array of zeros
		// (assuming Java will initialize metrics[] with zeros)
		if (entry != null)
		{			
			for(DataPlotEntry e : entry)
			{
				metrics[e.tid] = e.metval;
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

		return null;
	}

	@Override
	public BaseMetric[] getMetrics() {
		if (experiment == null)
			return null;
		
		Experiment exp = (Experiment) experiment;
		BaseMetric []metrics = exp.getMetrics();
		
		MetricRaw []rawMetrics = new MetricRaw[metrics.length];
		
		for(int i=0; i<metrics.length; i++) {
			BaseMetric m = metrics[i];
			rawMetrics[i] = new MetricRaw(m.getIndex(), m.getDisplayName(), m.getDescription(), 
										  null, i, m.getPartner(), m.getMetricType(), 
										  metrics.length);
		}
		return rawMetrics;
	}
}
