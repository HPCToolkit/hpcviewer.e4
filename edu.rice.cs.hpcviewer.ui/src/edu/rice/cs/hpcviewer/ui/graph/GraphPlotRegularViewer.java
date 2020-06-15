package edu.rice.cs.hpcviewer.ui.graph;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.swtchart.IAxisSet;
import org.swtchart.IAxisTick;

import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class GraphPlotRegularViewer extends GraphPlotViewer 
{
	static public final String ID    = "edu.rice.cs.hpcviewer.ui.partdescriptor.graph.plot";
	static public final String LABEL = "Plot grah";

	@Override
	protected String getXAxisTitle() {
		IAxisSet axisSet = this.getChart().getAxisSet();
		IAxisTick xTick  = axisSet.getXAxis(0).getTick();
		String title 	 = "Rank";

		xTick.setFormat(new DecimalFormat("##########"));
		IThreadDataCollection threadData = getInput().getThreadData();
		
		try {
			title = threadData.getRankTitle();
			
			if (threadData.getParallelismLevel()>1) 
			{
				xTick.setFormat(new DecimalFormat("######00.00##"));
				return title;
			}
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return title;
	}

	@Override
	protected double[] getValuesX(Scope scope, MetricRaw metric) throws NumberFormatException, IOException {
		IThreadDataCollection threadData = getInput().getThreadData();
		return threadData.getEvenlySparseRankLabels();
	}

	@Override
	protected double[] getValuesY(Scope scope, MetricRaw metric) throws IOException {
		IThreadDataCollection threadData = getInput().getThreadData();
		double []y_values = threadData.getMetrics(scope.getCCTIndex(), metric.getRawID(), metric.getSize());
		return y_values;
	}

	@Override
	protected ArrayList<Integer> translateUserSelection(
			ArrayList<Integer> selections) {
		return selections;
	}
	
	@Override
	public String getPartDescriptorId() {
		return ID;
	}

	@Override
	protected String getGraphTypeLabel() {
		return LABEL;
	}
	
}
