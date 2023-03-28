package edu.rice.cs.hpcviewer.ui.graph;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.extensions.charts.*;

/********************************************************************************
 * 
 * A customized extension of InteractiveChart for intercepting and interpreting
 * mouse click
 *
 ********************************************************************************/
public class GraphChart extends InteractiveChart implements MouseMoveListener
{
	GraphEditorInput input;
	
    public GraphChart(Composite parent, int style) {
		super(parent, style);
		
		getPlotArea().addMouseMoveListener(this);
	}

    
    public void setInput(GraphEditorInput input) {
    	this.input = input;
    }
    
	@Override
	public void mouseMove(MouseEvent e) {
		
		for(ISeries<?> series: getSeriesSet().getSeries()) {
			if (series instanceof ILineSeries<?>) {
				for(int i=0; i<series.getYSeries().length; i++) {
					Point p = series.getPixelCoordinates(i);
					double distance = Math.sqrt(Math.pow(e.x - p.x, 2) + Math.pow(e.y - p.y, 2));
					if(distance < ((ILineSeries<?>)series).getSymbolSize()) {
						String xVal = "x-value";
						
						if (input != null) {
							var idtype = input.getScope().getExperiment().getIdTupleType();
							xVal = (String) input.getThreadData().getIdTupleLabelWithoutGPU(idtype)[i];
						}
						
						var yVal = series.getYSeries()[i];
						var format = getAxisSet().getYAxis(0).getTick().getFormat();
						var yStr = format.format(yVal);
						
						getPlotArea().setToolTipText(xVal + " : " + yStr);
						return;
					}
				}
			}
		}
		getPlotArea().setToolTipText(null);
	}
}
