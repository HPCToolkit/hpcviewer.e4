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
    private boolean mouse = true;

    
    public ResizeListener(ScopeTreeTable table) {
    	this.table = table;
	}
    
	@Override
	public void handleEvent(Event event) {
        mouse = event.type == SWT.MouseUp;
	}

	@Override
	public void run() {
		if ((lastEvent + 500) < System.currentTimeMillis() && mouse) {
			// fix issue #199: at least one metric column is visible when resizing the app
			table.pack(1);
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
