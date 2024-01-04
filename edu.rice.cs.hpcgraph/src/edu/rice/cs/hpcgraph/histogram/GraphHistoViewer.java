package edu.rice.cs.hpcgraph.histogram;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.IAxis;
import org.eclipse.swtchart.IAxisSet;
import org.eclipse.swtchart.IAxisTick;
import org.eclipse.swtchart.IBarSeries;
import org.eclipse.swtchart.ISeries.SeriesType;

import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.metric.MetricRaw;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcgraph.GraphEditorInput;
import edu.rice.cs.hpcgraph.internal.AbstractGraphViewer;
import edu.rice.cs.hpcgraph.internal.GraphChart;
import edu.rice.cs.hpcgraph.internal.IGraphTranslator;
import edu.rice.cs.hpcgraph.internal.IdentityGraphTranlator;


public class GraphHistoViewer extends AbstractGraphViewer 
{
	public GraphHistoViewer(CTabFolder tabFolder, int style) {
		super(tabFolder, style);
	}

	public static final String LABEL = "Histogram graph";

	@Override
	protected int plotData(GraphEditorInput input) {
		final int bins = 10;
		
		final Scope scope = input.getScope();
		final var metric = input.getMetric();
		double y_values[], x_values[];
		
		// new meta.db database: the id is from metric index,
		// and we don't need the size of metrics
		// set to zero is fine.
		int id = metric.getIndex();
		// in case of old database, the metric is from MetricRaw
		// the information of the size is available in metric raw.
		if (metric instanceof MetricRaw) {
			id = ((MetricRaw) metric).getRawID();
		}
		
		try {
			IThreadDataCollection threadData = input.getThreadData();
			y_values = threadData.getMetrics(scope.getCCTIndex(), id);

		} catch (Exception e) {
			Display display = Display.getDefault();
			MessageDialog.openError(display.getActiveShell(), "Error reading file !", e.getMessage());
			return -1;
		}			

		Histogram histo = new Histogram(bins, y_values);
		y_values = histo.getAxisY();
		x_values = histo.getAxisX();
		double min = histo.min();
		double max = histo.max();
		double single = 0.1 * (max-min)/bins;

		Chart chart = getChart();
		
		IAxisSet axisSet = chart.getAxisSet();
		IAxisTick xTick  = axisSet.getXAxis(0).getTick();
		xTick.setFormat(GraphChart.METRIC_FORMAT);
		
		IAxisTick yTick = axisSet.getYAxis(0).getTick();
		yTick.setFormat(new DecimalFormat("#######"));

		IAxis axis = axisSet.getXAxis(0); 
		axis.getRange().lower = min - single;
		axis.getRange().upper = max + single;
		
		// create scatter series
		var scatterSeries = (IBarSeries<?>) chart.getSeriesSet()
				.createSeries(SeriesType.BAR, metric.getDisplayName() );
		scatterSeries.setXSeries(x_values);
		scatterSeries.setYSeries(y_values);

		chart.getAxisSet().getXAxis(0).getTitle().setText( "Metric Value" );
		chart.getAxisSet().getYAxis(0).getTitle().setText( "Frequency" );
		
		return 0;
	}

	@Override
	protected ArrayList<Integer> translateUserSelection(ArrayList<Integer> selections) {
		return selections;
	}
	

	@Override
	protected IGraphTranslator getGraphTranslator() {
		return new IdentityGraphTranlator();
	}


	@Override
	protected String getGraphTypeLabel() {
		return LABEL;
	}

}
