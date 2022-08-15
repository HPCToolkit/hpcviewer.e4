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


/********************************************************************
 * 
 * Class to plot a graph
 *
 *********************************************************************/
public class GraphPlotRegularViewer extends AbstractGraphPlotViewer 
{
	public GraphPlotRegularViewer(CTabFolder tabFolder, int style) {
		super(tabFolder, style);
	}

	public static final String LABEL = "Plot graph";

	@Override
	protected String getXAxisTitle() {
		IAxisSet axisSet = this.getChart().getAxisSet();
		IAxisTick xTick  = axisSet.getXAxis(0).getTick();
		String title 	 = "Rank";

		xTick.setFormat(new DecimalFormat("##########"));
		IThreadDataCollection threadData = getInput().getThreadData();
		
		try {
			title = threadData.getRankTitle();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return title;
	}

	@Override
	protected double[] getValuesX(Scope scope, BaseMetric metric) throws NumberFormatException, IOException {
		IThreadDataCollection threadData = getInput().getThreadData();
		return threadData.getEvenlySparseRankLabels();
	}
	
	
	@Override
	protected int setupXAxis(GraphEditorInput input, ILineSeries scatterSeries) {
		try {
			Scope scope = input.getScope();
			BaseMetric metric = input.getMetric();

			double [] valuesX = getValuesX(scope, metric);
			scatterSeries.setXSeries(valuesX);
			
		} catch (NumberFormatException | IOException e) {
			showErrorMessage(e);
			return PLOT_ERR_UNKNOWN;
		}
		return PLOT_OK;
	}

	@Override
	protected double[] getValuesY(Scope scope, BaseMetric metric) throws Exception {
		
		int id = metric.getIndex();
		int size = 0;
		
		// in case of old database, the metric is from MetricRaw
		if (metric instanceof MetricRaw) {
			id = ((MetricRaw) metric).getRawID();
			size = ((MetricRaw)metric).getSize();
		}
		IThreadDataCollection threadData = getInput().getThreadData();
		return threadData.getMetrics(scope.getCCTIndex(), id, size);
	}

	@Override
	protected ArrayList<Integer> translateUserSelection(
			ArrayList<Integer> selections) {
		return selections;
	}
	


	@Override
	protected String getGraphTypeLabel() {
		return LABEL;
	}
	
}
