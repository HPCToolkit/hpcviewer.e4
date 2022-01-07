package edu.rice.cs.hpcviewer.ui.graph;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.IAxis.Direction;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ext.InteractiveChart;

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
		getPlotArea().addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {}
			
			@Override
			public void mouseDown(MouseEvent e) {
				if (chartListener != null)
					handleMouseDown(e);
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {}
		});
	}

    private void handleMouseDown(MouseEvent event) 
    {
    	if (event.button == 1) {
    		if (chartListener != null) {
                // get the closest data to the cursor:q
                UserSelectionData result = null, tmp_result = null;
    			int symbolSize = 0;

    			// relating the cursor position with the data
                for (IAxis axis : getAxisSet().getAxes()) {
                	if (axis.getDirection() == Direction.X) {
                		// x-axis
                		double x = axis.getDataCoordinate(event.x);
                		tmp_result = findDataX(x);
            			if (tmp_result.serie.getType() == SeriesType.LINE) {
            				symbolSize = ((ILineSeries)tmp_result.serie).getSymbolSize();
            				int x_pixel = axis.getPixelCoordinate(tmp_result.valueX);
            				if (Math.abs(event.x-x_pixel) > symbolSize)
            					tmp_result =null;
            			}

                	} else if (axis.getDirection() == Direction.Y)
                	{	// y-axis
                		if (tmp_result == null)
                			continue;

                		ISeries []series = getSeriesSet().getSeries();
                		for (ISeries serie : series)
                		{
                			double []y_values = serie.getYSeries();
                			double y_min = axis.getDataCoordinate(event.y-symbolSize);
                			double y_max = axis.getDataCoordinate(event.y+symbolSize);
                			
                			int index = tmp_result.index;
                			if (Double.isNaN(y_values[index]) ) {
                				// check the lower value
                				for (; Double.isNaN(y_values[index]) && y_values[index] > y_min; index--);
                				// check the upper value
                				if (Double.isNaN(y_values[index])) {
                					index = tmp_result.index + 1;
                					for (; index<y_values.length && Double.isNaN(y_values[index]) && y_values[index] < y_max
                							; index++);
                					if (index >= y_values.length)
                						index = y_values.length - 1;
                				}
                			}                			
                			if (! Double.isNaN(y_values[index])) {
                    			// double check
                				int y_coord   = axis.getPixelCoordinate(y_values[index]);
                				if (Math.abs(y_coord-event.y)<symbolSize) {
                					result		  = tmp_result;
                    				result.valueY = y_values[index];
                    				result.valueX = x_values[index];
                    				result.serie  = serie;
                    				result.event  = event;
                    				result.index  = index;
                    				resetSelection();
                    				
                                	chartListener.selection(result);
                                	return;
                				}
                			}
                		}
                	}
                }
            }
    	}
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
    	result.serie = serie;
    	return result;
    }
}
