package edu.rice.cs.hpctraceviewer.ui.main;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.context.BaseTraceContext;

abstract public class AbstractAxisCanvas extends Canvas 
				implements PaintListener, IOperationHistoryListener, DisposeListener
{
	final ITracePart tracePart;
	
	public AbstractAxisCanvas(ITracePart tracePart, Composite parent, int style) {
		super(parent, SWT.NO_BACKGROUND | style);
		
		this.tracePart = tracePart;

		addPaintListener(this);
		addDisposeListener(this);
	}

	

	@Override
	public void setData(Object data) {
		
		if (getData() == null) {
			// just initialize once
			tracePart.getOperationHistory().addOperationHistoryListener(this);
		}

		super.setData(data);
		
		syncRedraw();
	}

	
	@Override
	public void widgetDisposed(DisposeEvent e) {
		tracePart.getOperationHistory().removeOperationHistoryListener(this);
	}

		
	/***
	 * Just like normal redraw, but this method uses UI thread
	 * and wait until the thread is available
	 */
	protected void syncRedraw() {
		
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				if (getData() == null)
					return;
				
				redraw();
			}
		});
	}



	@Override
	public void historyNotification(OperationHistoryEvent event) {

		if (event.getEventType() == OperationHistoryEvent.DONE) {
			final IUndoableOperation operation = event.getOperation();
			
			IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_BUFFER);

			if (operation.hasContext(context)) {
				syncRedraw();
			}
		}
	}

}
