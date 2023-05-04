package edu.rice.cs.hpcviewer.ui.graph;

import java.text.DecimalFormat;
import java.util.Arrays;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.extensions.charts.*;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;

/********************************************************************************
 * 
 * A customized extension of InteractiveChart for intercepting and interpreting
 * mouse click
 *
 ********************************************************************************/
public class GraphChart extends InteractiveChart implements MouseMoveListener
{
	public static final DecimalFormat METRIC_FORMAT = new DecimalFormat("0.0##E0##");
	
	private IdTupleType idTupleType;
	private IThreadDataCollection threadData;
	
	public GraphChart(Composite parent, int style) {
		super(parent, style);
		
		getPlotArea().addMouseMoveListener(this);
	}

    
    public void setInput(GraphEditorInput input) {
    	idTupleType = input.getScope().getExperiment().getIdTupleType();
    	threadData  = input.getThreadData();
    }
    
	@Override
	public void mouseMove(MouseEvent e) {
		var seriesSet = getSeriesSet();
		
		// check for empty plot (no metrics)
		if (seriesSet == null || 
			seriesSet.getSeries() == null || 
			seriesSet.getSeries().length == 0)
			return;
		
		var series  = seriesSet.getSeries()[0];
		var xSeries = series.getXSeries();
		
		var symSize = ((ILineSeries<?>)series).getSymbolSize();
		
		var axisSet = getAxisSet();
		var xAxis = axisSet.getXAxes()[0];
		var x = xAxis.getDataCoordinate(e.x);
		
		var xIndex = Arrays.binarySearch(xSeries, x);

		if (xIndex < 0) {
			// in case the sorting can't find the EXACT value,
			// we start from the previous x index
			xIndex = Math.min(-1 * xIndex - 1, xSeries.length-1);
		}
		int xDistance = 0;
		int maxIndex = xIndex;
		while(xDistance <= symSize*2 && maxIndex < xSeries.length) {
			var p = series.getPixelCoordinates(maxIndex);
			xDistance = Math.abs(p.x - e.x);
			
			double distance = Math.sqrt(Math.pow(e.x - p.x, 2) + Math.pow(e.y - p.y, 2));
			if (distance <= symSize) {
				var ySeries = series.getYSeries();
				setTooltip(maxIndex, ySeries);
				return;
			}
			maxIndex++;
		}
	}
	
	
	private void setTooltip(int index, double[] ySeries) {
		var idt = threadData.getIdTupleLabelWithoutGPU(idTupleType)[index];

		var formattedValue = METRIC_FORMAT.format(ySeries[index]);
		var output = String.format("%s: %s", idt, formattedValue);
		
		getPlotArea().setToolTipText(output);
	}
}
