package edu.rice.cs.hpcviewer.ui.graph;

import java.io.IOException;
import javax.inject.Inject;

import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.CTabFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.IAxisSet;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.ISeries.SeriesType;
import org.eclipse.swtchart.LineStyle;
import org.eclipse.swtchart.Range;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public abstract class AbstractGraphPlotViewer extends AbstractGraphViewer 
{
	private static final int DEFAULT_DIAMETER = 3;

	@Inject EPartService partService;
	@Inject DatabaseCollection database;
	
	protected AbstractGraphPlotViewer(CTabFolder tabFolder, int style) {
		super(tabFolder, style);
	}

	@Override
	protected int plotData(GraphEditorInput input) {
		
		Scope scope = input.getScope();
		BaseMetric metric = input.getMetric();
		
		// -----------------------------------------------------------------
		// gather x and y values
		// -----------------------------------------------------------------
		
		double []valuesY = null;

		try {
			valuesY = getValuesY(scope, metric);
			
		} catch (Exception e) {
			showErrorMessage(e);
			return PLOT_ERR_UNKNOWN;
		}

		Chart chart = getChart();

		// -----------------------------------------------------------------
		// create scatter series
		// -----------------------------------------------------------------
		ILineSeries scatterSeries = (ILineSeries) chart.getSeriesSet()
				.createSeries(SeriesType.LINE, input.toString() );
		
		scatterSeries.setLineStyle( LineStyle.NONE);
		scatterSeries.setSymbolSize(DEFAULT_DIAMETER);
		
		// -----------------------------------------------------------------
		// set x-axis
		// -----------------------------------------------------------------

		String axisX = this.getXAxisTitle( );
		chart.getAxisSet().getXAxis(0).getTitle().setText( axisX );
		chart.getAxisSet().getYAxis(0).getTitle().setText( "Metric Value" );
		
		IAxisSet axisSet = chart.getAxisSet();
		
		// -----------------------------------------------------------------
		// set the values x and y to the plot
		// -----------------------------------------------------------------
		setupXAxis(input, scatterSeries);
		scatterSeries.setYSeries(valuesY);

		// set the lower range to be zero so that we can see if there is load imbalance or not
		
		Range range = axisSet.getAxes()[1].getRange();
		if (range.lower > 0) {
			range.lower = 0;
			axisSet.getAxes()[1].setRange(range);
		}
		
		return PLOT_OK;
	}


	/***
	 * Generic error message
	 * @param e Exception
	 */
	protected void showErrorMessage(Exception e) {
		
		String label = "Error while opening thread level data metric file";
		
		MessageDialog.openError(getChart().getShell(), label, e.getMessage());
		
		Logger logger = LoggerFactory.getLogger(getClass());
		logger.error(label, e);
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
	protected abstract double[] getValuesX(Scope scope, BaseMetric metric) throws NumberFormatException, IOException;
	
	/*****
	 * retrieve the value of Y
	 * @param objManager
	 * @param scope
	 * @param metric
	 * @return
	 * @throws Exception 
	 */
	protected abstract double[] getValuesY(Scope scope, BaseMetric metric)
			 throws  Exception;


	/****
	 * Set the x-axis
	 * @param input
	 * @param scatterSeries
	 * @return
	 */
	protected abstract int setupXAxis(GraphEditorInput input, ILineSeries scatterSeries);
}
