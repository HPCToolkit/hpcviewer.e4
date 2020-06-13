package edu.rice.cs.hpcviewer.ui.graph;

import java.io.IOException;
import java.util.ArrayList;

import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class GraphPlotSortViewer extends GraphPlotViewer 
{
	static public final String ID = "edu.rice.cs.hpcviewer.ui.partdescriptor.graph.sort";
	
    private PairThreadIndex []pairThreadIndex;

	@Override
	protected String getXAxisTitle() {
		return "Rank in Sorted Order";
	}

	@Override
	protected double[] getValuesX(Scope scope, MetricRaw metric) throws NumberFormatException, IOException {
		IThreadDataCollection threadData = getInput().getThreadData();
		double x_values[] = threadData.getRankLabels();
		double sequence_x[] = new double[x_values.length];
		for (int i=0; i<x_values.length; i++) {
			sequence_x[i] = (double) i;
		}
		return sequence_x;
	}

	@Override
	protected double[] getValuesY(Scope scope, MetricRaw metric) throws IOException {

		IThreadDataCollection threadData = getInput().getThreadData();
		double y_values[] = null;
		y_values = threadData.getMetrics(scope.getCCTIndex(),metric.getRawID(), metric.getSize());
		pairThreadIndex = new PairThreadIndex[y_values.length];

		for(int i=0; i<y_values.length; i++)
		{
			pairThreadIndex[i] = new PairThreadIndex();
			pairThreadIndex[i].index = i;
			pairThreadIndex[i].value = y_values[i];
		}
		java.util.Arrays.sort(y_values);
		java.util.Arrays.sort(pairThreadIndex);
		
		return y_values;
	}

	@Override
	public String getPartDescriptorId() {
		return ID;
	}
	
	/*************
	 * 
	 * Pair of thread and the sequential index for the sorting
	 *
	 *************/
	static private class PairThreadIndex implements Comparable<PairThreadIndex>
	{
		int index;
		double value;

		@Override
		public int compareTo(PairThreadIndex o) {
			if (value > o.value)
				return 1;
			else if (value < o.value)
				return -1;
			return 0;
		}
		
		@Override
		public String toString() {
			return "(" + index + "," + value + ")";
		}
	}


	@Override
	protected ArrayList<Integer> translateUserSelection(ArrayList<Integer> selections) {
		
		if (pairThreadIndex != null) {
			ArrayList<Integer> list = new ArrayList<Integer>( selections.size());
			for(Integer i : selections) {
				list.add(pairThreadIndex[i].index);
			}
			return list;
		}
		return null;
	}
}
