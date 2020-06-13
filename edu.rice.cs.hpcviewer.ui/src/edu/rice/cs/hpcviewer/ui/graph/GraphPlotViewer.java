package edu.rice.cs.hpcviewer.ui.graph;

import java.io.IOException;
import java.text.DecimalFormat;
import javax.inject.Inject;

import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.IAxisSet;
import org.swtchart.IAxisTick;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.LineStyle;
import org.swtchart.Range;

import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public abstract class GraphPlotViewer extends GraphViewer 
{

	static private final int DEFAULT_DIAMETER = 3;

	@Inject EPartService partService;
	
	public GraphPlotViewer() {
		super();
	}

	@Override
	protected int plotData(GraphEditorInput input) {
		
		Scope scope = input.getScope();
		MetricRaw metric = (MetricRaw) input.getMetric();
		
		double y_values[];
		try {
			y_values = getValuesY(scope, metric);
			
			double []x_values;

			x_values = getValuesX(scope, metric);

			Chart chart = getChart();

			// -----------------------------------------------------------------
			// create scatter series
			// -----------------------------------------------------------------
			ILineSeries scatterSeries = (ILineSeries) chart.getSeriesSet()
					.createSeries(SeriesType.LINE, metric.getDisplayName() );
			
			scatterSeries.setLineStyle( LineStyle.NONE);
			scatterSeries.setSymbolSize(DEFAULT_DIAMETER);
			
			// -----------------------------------------------------------------
			// set the values x and y to the plot
			// -----------------------------------------------------------------
			scatterSeries.setXSeries(x_values);
			scatterSeries.setYSeries(y_values);
			
			String axis_x = this.getXAxisTitle( );
			chart.getAxisSet().getXAxis(0).getTitle().setText( axis_x );
			chart.getAxisSet().getYAxis(0).getTitle().setText( "Metric Value" );
			
			final IAxisTick xTick = chart.getAxisSet().getXAxis(0).getTick();
			xTick.setFormat(new DecimalFormat("#############"));
			
			// adjust the axis range
			editorFinalize();

		} catch (IOException e) {
			Display display = Display.getDefault();
			
			MessageDialog.openError(display.getActiveShell(), "Fail to open the file", 
					"Error while openging the file: " + e.getMessage());
			return -2;
			
		} catch (Exception e) {
			Display display = Display.getDefault();
			MessageDialog.openError(display.getActiveShell(), "Fail to display the graph", 
					 "Error while opening thread level data metric file.\n"+ e.getMessage());
			
			return -1;
		}
		return 0;
	}

	
	/******
	 * Do finalization of the editor
	 * 
	 * Due to SWT Chart bug, we need to adjust the range once the create-part-control
	 * 	finishes its layout.
	 */
	private void editorFinalize() {
		IAxisSet axisSet = this.getChart().getAxisSet();
		axisSet.adjustRange();

		// set the lower range to be zero so that we can see if there is load imbalance or not
		Range range = axisSet.getAxes()[1].getRange();
		if (range.lower > 0) {
			range.lower = 0;
			axisSet.getAxes()[1].setRange(range);
		}
	}


	/***
	 * retrieve the title of the X axis
	 * @param type
	 * @return
	 */
	protected abstract String getXAxisTitle();

	/*****
	 * retrieve the value of Xs
	 * @param objManager
	 * @param scope
	 * @param metric
	 * @return
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	protected abstract double[] getValuesX(Scope scope, MetricRaw metric) throws NumberFormatException, IOException;
	
	/*****
	 * retrieve the value of Y
	 * @param objManager
	 * @param scope
	 * @param metric
	 * @return
	 */
	protected abstract double[] getValuesY(Scope scope, MetricRaw metric)
			 throws IOException;


}
