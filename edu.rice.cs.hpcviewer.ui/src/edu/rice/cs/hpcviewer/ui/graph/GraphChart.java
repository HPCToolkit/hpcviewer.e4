package edu.rice.cs.hpcviewer.ui.graph;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swtchart.extensions.charts.*;

/********************************************************************************
 * 
 * A customized extension of InteractiveChart for intercepting and interpreting
 * mouse click
 *
 ********************************************************************************/
public class GraphChart extends InteractiveChart 
{
    public GraphChart(Composite parent, int style) {
		super(parent, style);
	}

    @Override
	public void handleEvent(Event event) {

		super.handleEvent(event);
		switch(event.type) {
			case SWT.MouseDown:
				//
				break;
			default:
				break;
		}
	}


    public void setChartSelectionListener(IChartSelectionListener listener)
    {
    }
}
