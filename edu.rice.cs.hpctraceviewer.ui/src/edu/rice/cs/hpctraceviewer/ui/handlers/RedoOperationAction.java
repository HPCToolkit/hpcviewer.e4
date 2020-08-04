package edu.rice.cs.hpctraceviewer.ui.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.context.BaseTraceContext;


public class RedoOperationAction  
{


	@Execute
	protected void execute(MPart part) {

		ITracePart tracePart = (ITracePart) part.getObject();
		IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_TRACE);
		IUndoableOperation[] undos = tracePart.getOperationHistory().getUndoHistory(context);

		if (undos.length == 0) {
			// hack: when there's no undo, we need to remove the current
			// history into the undo stack. To do this properly, we
			// should perform an extra redo before the real redo

			//doRedo();
			IUndoableOperation[] operations = tracePart.getOperationHistory().getRedoHistory(context);

			if (operations != null && operations.length-2>=0) {
				execute(tracePart, operations[operations.length-2]);
				return;
			}
		}
		doRedo(tracePart);
	}

	
	@CanExecute
	protected boolean canExecute(MPart part) {

		ITracePart tracePart = (ITracePart) part.getObject();
		IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_TRACE);

		final IUndoableOperation []ops = tracePart.getOperationHistory().getRedoHistory(context); 
		boolean status = ops.length > 0;

		return status;
	}
	
	
	/****
	 * helper method to perform the default redo
	 */
	private void doRedo(ITracePart tracePart) {
		IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_TRACE);
		try {
			IStatus status = tracePart.getOperationHistory().redo(context, null, null);
			if (!status.isOK()) {
				System.err.println("Cannot redo: " + status.getMessage());
			}
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
	

	protected void execute(ITracePart tracePart, IUndoableOperation operation) {
		try {
			tracePart.getOperationHistory().redoOperation(operation, null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
}
