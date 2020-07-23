package edu.rice.cs.hpcviewer.ui.graph;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisSet;
import org.swtchart.IAxisTick;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries.SeriesType;

import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpcviewer.ui.util.Constants;

public class GraphHistoViewer extends AbstractGraphViewer 
{
	public GraphHistoViewer(CTabFolder tabFolder, int style) {
		super(tabFolder, style);
	}

	static public final String LABEL = "Histogram graph";

	@Override
	protected int plotData(GraphEditorInput input) {
		final int bins = 10;
		
		final Scope scope = input.getScope();
		final MetricRaw metric = (MetricRaw) input.getMetric();
		double y_values[], x_values[];
		try {
			IThreadDataCollection threadData = input.getThreadData();
			y_values = threadData.getMetrics(scope.getCCTIndex(), metric.getRawID(), metric.getSize());

		} catch (IOException e) {
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
		xTick.setFormat(new DecimalFormat("0.###E0##"));
		
		IAxisTick yTick = axisSet.getYAxis(0).getTick();
		yTick.setFormat(new DecimalFormat("#######"));

		IAxis axis = axisSet.getXAxis(0); 
		axis.getRange().lower = min - single;
		axis.getRange().upper = max + single;
		
		// create scatter series
		IBarSeries scatterSeries = (IBarSeries) chart.getSeriesSet()
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
	public String getPartDescriptorId() {
		return Constants.ID_GRAPH_HISTO;
	}

	@Override
	protected String getGraphTypeLabel() {
		return LABEL;
	}

}
