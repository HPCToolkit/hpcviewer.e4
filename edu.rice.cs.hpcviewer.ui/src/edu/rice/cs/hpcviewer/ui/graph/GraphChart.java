package edu.rice.cs.hpcviewer.ui.graph;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swtchart.IAxis;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.IAxis.Direction;
import org.eclipse.swtchart.ISeries.SeriesType;
import org.eclipse.swtchart.extensions.charts.*;

/********************************************************************************
 * 
 * A customized extension of InteractiveChart for intercepting and interpreting
 * mouse click
 *
 ********************************************************************************/
public class GraphChart extends InteractiveChart 
{
    /** listener for a select event in the chart */
    private IChartSelectionListener chartListener;

    public GraphChart(Composite parent, int style) {
		super(parent, style);
	}

    private void handleMouseDown(MouseEvent event) 
    {

    }

    public void setChartSelectionListener(IChartSelectionListener listener)
    {
    	chartListener = listener;
    }
    
    /** assuming all series have the same values, this variable stores the x values**/
    private double []x_values = null;
    
    /***
     * find the closest x value from the x axis
     * this doens't guarantee if x is really the same to the x value
     * @param value
     * @return
     */
    private UserSelectionData findDataX(double value)
    {
    	ISeries serie = getSeriesSet().getSeries()[0];
    	if (x_values == null) {
    		// assume all series have the same x values
    		x_values = serie.getXSeries();
    	}
    	int index = java.util.Arrays.binarySearch(x_values, value);
    	
    	UserSelectionData result = new UserSelectionData();
    	
    	result.index = index;
    	result.valueX = x_values[index];
    	return result;
    }
}
