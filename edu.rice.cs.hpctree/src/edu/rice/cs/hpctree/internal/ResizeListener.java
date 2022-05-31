package edu.rice.cs.hpctree.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import edu.rice.cs.hpctree.ScopeTreeTable;

public class ResizeListener implements ControlListener, Runnable, Listener 
{
	private final ScopeTreeTable table;
	
    private long lastEvent = 0;
    private boolean mouse = false;
    private int lastWidth  = 0;
    
    public ResizeListener(ScopeTreeTable table) {
    	this.table = table;
	}
    
	@Override
	public void handleEvent(Event event) {
		if (event.type == SWT.MouseDown)
	    	lastWidth  = table.getTable().getSize().x;
		
        mouse = event.type == SWT.MouseUp;
	}

	@Override
	public void run() {
		if ((lastEvent + 500) < System.currentTimeMillis() && mouse) {
			// fix issue #199: Make sure we pack the columns only when we resize the table
			// btw, due to unknown issue on SWT or Linux/GTK or both, check manually if resizing occurs 
			// working on SWT is highly frustrating :-(
	    	var width = table.getTable().getSize().x;
	    	if (width != lastWidth) {
	    		table.pack();
	    		lastWidth  = table.getTable().getSize().x;
	    	}
		} else {
            Display.getDefault().timerExec(500, this);
		}
	}

	@Override
	public void controlMoved(ControlEvent e) {}

	@Override
	public void controlResized(ControlEvent e) {
        lastEvent = System.currentTimeMillis();
        Display.getDefault().timerExec(500, this);
	}

}
