package edu.rice.cs.hpctraceviewer.ui.internal;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class ResizeListener implements ControlListener, Runnable, Listener 
{
	final private BufferPaint buffer;
    private long lastEvent = 0;
    private boolean mouse = true;

    public ResizeListener(BufferPaint buffer) {
    	this.buffer = buffer;
    }
    
    public void controlMoved(ControlEvent e) {
    }

    public void controlResized(ControlEvent e) {
        lastEvent = System.currentTimeMillis();
        Display.getDefault().timerExec(500, this);
    }

    public void run() {
        if ((lastEvent + 500) < System.currentTimeMillis() && mouse) 
        {
        	buffer.rebuffering();
        } else {
            Display.getDefault().timerExec(500, this);
        }
    }
    public void handleEvent(Event event) {
        mouse = event.type == SWT.MouseUp;
    }

}
