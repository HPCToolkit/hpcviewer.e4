package edu.rice.cs.hpcviewer.ui.graph;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.eclipse.swt.custom.CTabFolder;
import org.swtchart.IAxisSet;
import org.swtchart.IAxisTick;
import org.swtchart.ILineSeries;

import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpcdata.tld.collection.ThreadDataCollection2;
import edu.rice.cs.hpcdata.tld.collection.ThreadDataCollection3;


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
	protected double[] getValuesX(Scope scope, BaseMetric metric) throws NumberFormatException, IOException {
		IThreadDataCollection threadData = getInput().getThreadData();
		return threadData.getEvenlySparseRankLabels();
	}
	
	
	@Override
	protected int setupXAxis(GraphEditorInput input, ILineSeries scatterSeries) {
		IAxisSet axisSet = getChart().getAxisSet();
		IThreadDataCollection threadData = input.getThreadData();
		
		if (threadData instanceof ThreadDataCollection2) {			
			try {
				final IAxisTick xTick = axisSet.getXAxis(0).getTick();
				xTick.setFormat(new DecimalFormat("#############"));

				Scope scope = input.getScope();
				BaseMetric metric = input.getMetric();

				double [] x_values = getValuesX(scope, metric);
				scatterSeries.setXSeries(x_values);
				
			} catch (NumberFormatException | IOException e) {
				showErrorMessage(e);
				return PLOT_ERR_UNKNOWN;
			}
		} else {
			ThreadDataCollection3 data = (ThreadDataCollection3) threadData;
			try {
				
				String []labels = data.getRankStringLabels();
				axisSet.getXAxis(0).enableCategory(true);
				axisSet.getXAxis(0).setCategorySeries(labels);
				axisSet.getXAxis(0).getTick().setTickLabelAngle(45);
				
			} catch (IOException e) {
				showErrorMessage(e);
				return PLOT_ERR_UNKNOWN;
			}
		}
		return PLOT_OK;
	}

	@Override
	protected double[] getValuesY(Scope scope, BaseMetric metric) throws IOException {
		
		int id = metric.getIndex();
		int size = 0;
		
		// in case of old databae, the metric is from MetricRaw
		if (metric instanceof MetricRaw) {
			id = ((MetricRaw) metric).getRawID();
			size = ((MetricRaw)metric).getSize();
		}
		IThreadDataCollection threadData = getInput().getThreadData();
		double []y_values = threadData.getMetrics(scope.getCCTIndex(), id, size);
		return y_values;
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
