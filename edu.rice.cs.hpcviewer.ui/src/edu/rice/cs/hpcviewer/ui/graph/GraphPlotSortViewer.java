package edu.rice.cs.hpcviewer.ui.graph;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.eclipse.swt.custom.CTabFolder;
import org.swtchart.IAxisSet;
import org.swtchart.IAxisTick;
import org.swtchart.ILineSeries;

import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricRaw;
import edu.rice.cs.hpcdata.experiment.scope.Scope;


public class GraphPlotSortViewer extends AbstractGraphPlotViewer 
{
	public GraphPlotSortViewer(CTabFolder tabFolder, int style) {
		super(tabFolder, style);
	}

	static public final String LABEL = "Sorted plot graph";

    private PairThreadIndex []pairThreadIndex;

	@Override
	protected String getXAxisTitle() {
		return "Rank in Sorted Order";
	}

	@Override
	protected double[] getValuesX(Scope scope, BaseMetric metric) throws NumberFormatException, IOException {
		IThreadDataCollection threadData = getInput().getThreadData();
		double x_values[] = threadData.getRankLabels();
		double sequence_x[] = new double[x_values.length];
		for (int i=0; i<x_values.length; i++) {
			sequence_x[i] = (double) i;
		}
		return sequence_x;
	}

	@Override
	protected double[] getValuesY(Scope scope, BaseMetric metric) throws Exception {

		int id = metric.getIndex();
		int size = 0;
		
		if (metric instanceof MetricRaw) {
			id = ((MetricRaw) metric).getRawID();
			size = ((MetricRaw) metric).getSize();
		}
		
		IThreadDataCollection threadData = getInput().getThreadData();
		double y_values[] = null;
		y_values = threadData.getMetrics(scope.getCCTIndex(), id, size);
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

	@Override
	protected String getGraphTypeLabel() {
		return LABEL;
	}

	@Override
	protected int setupXAxis(GraphEditorInput input, ILineSeries scatterSeries) {
		IAxisSet axisSet = getChart().getAxisSet();

		final IAxisTick xTick = axisSet.getXAxis(0).getTick();
		xTick.setFormat(new DecimalFormat("#############"));

		Scope scope = input.getScope();
		BaseMetric metric = input.getMetric();

		try {
			double[] x_values = getValuesX(scope, metric);
			scatterSeries.setXSeries(x_values);
			
		} catch (NumberFormatException | IOException e) {

			showErrorMessage(e);
			return PLOT_ERR_IO;
		}

		return PLOT_OK;
	}
}
