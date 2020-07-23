package edu.rice.cs.hpcviewer.ui.graph;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.eclipse.swt.custom.CTabFolder;
import org.swtchart.IAxisSet;
import org.swtchart.IAxisTick;

import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpcviewer.ui.util.Constants;

public class GraphPlotRegularViewer extends AbstractGraphPlotViewer 
{
	public GraphPlotRegularViewer(CTabFolder tabFolder, int style) {
		super(tabFolder, style);
		// TODO Auto-generated constructor stub
	}

	static public final String LABEL = "Plot graph";

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
		return Constants.ID_GRAPH_PLOT;
	}

	@Override
	protected String getGraphTypeLabel() {
		return LABEL;
	}
	
}
