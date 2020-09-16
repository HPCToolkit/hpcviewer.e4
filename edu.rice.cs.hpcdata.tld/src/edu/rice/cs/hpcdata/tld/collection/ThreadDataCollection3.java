package edu.rice.cs.hpcdata.tld.collection;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.rice.cs.hpc.data.db.DataSummary;
import edu.rice.cs.hpc.data.db.IdTuple;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.BaseExperiment.Db_File_Type;
import edu.rice.cs.hpc.data.experiment.extdata.AbstractThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.metric.version3.MetricValueCollection3;
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
	/** Number of parallelism level or number of levels in hierarchy */
	private int numLevels;
	private double[] labels;
	private String[] strLabels;
	
	@Override
	public void open(RootScope root, String directory) throws IOException {
		data_plot = new DataPlot();
		data_plot.open(directory + File.separatorChar + 
				BaseExperiment.getDefaultDatabaseName(Db_File_Type.DB_PLOT));
		
		MetricValueCollection3 col = (MetricValueCollection3) root.getMetricValueCollection(root);
		data_summary = col.getDataSummary();
	}

	@Override
	public boolean isAvailable() {
		return ((data_plot != null));
	}

	@Override
	public double[] getRankLabels() {		
		if (strLabels == null) 
			initTuples();
		
		return labels;
	}


	@Override
	public String[] getRankStringLabels() throws IOException {		
		if (strLabels == null) 
			initTuples();
		
		return strLabels;
	}

	@Override
	public int getParallelismLevel() {
		
		return numLevels;
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
		
		final int num_ranks 		= list.size();
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

		if (data_summary == null)
			return null;
		
		data_summary.getMetrics(thread_id, MetricIndex);
		return null;
	}

	
	
	private void initTuples() {
		List<IdTuple> listTuple = data_summary.getTuple();
		
		// 1. first try to compute the number of parallelism levels
		//    A level is part of label if its kind is not invariant
		//    For instance: if we have 0.0, 0.1, 0.2, ... 0.N then
		//                  the first is invariant, the second number will be part of label
		//                  it should become: 0, 1, 2, ... N
		//
		//
		Map<Short, Map<Long, Integer>> mapKindToMapIndex = new HashMap<Short, Map<Long,Integer>>();
		Map<Short, Boolean> mapKindToVariant = new HashMap<Short, Boolean>();
		
		numLevels = 0;

		for(int i=1; i<listTuple.size(); i++) {
			IdTuple tuple = listTuple.get(i);
			
			for (int j=0; j<tuple.length; j++) {
				Short kind = tuple.kind[j];
				Long index = tuple.index[j];
				
				Map<Long, Integer> map = mapKindToMapIndex.get(kind);
				
				if (map == null) {
					map = new HashMap<Long, Integer>();
					map.put(index, 1);
					mapKindToMapIndex.put(kind, map);
					
				} else {
					// this kind is not an invariant
					// the next step should keep track of this kind
					Integer count = map.get(index);
					if (count == null) {
						// this kind is a variant !
						mapKindToVariant.put(kind, Boolean.TRUE);
						numLevels++;
						
					} else {
						count++;
						map.put(index, count);
					}
				}
			}
		}

		// 2. next, store the number of parallelism levels
		// we remove the invariants from the list of labels
		
		labels    = new double[listTuple.size()-1];
		strLabels = new String[listTuple.size()-1];
		int idx=0;
		
		for(int i=1; i<listTuple.size(); i++) {
			IdTuple tuple = listTuple.get(i);
			
			short []kind = tuple.kind;
			long []index = tuple.index;
			
			String label = "";
			
			for(int j=0; j<kind.length; j++) {
				Short k = kind[j];
				
				Boolean variant = mapKindToVariant.get(k);
				if (variant == null)
					continue;
				
				if (label.length()>0)
					label += ".";

				Long ix = index[j];
				label += String.valueOf(ix);				
			}
			strLabels[idx] = label;
			labels[idx] = Double.valueOf(label);
			idx++;
		}
	}
}
