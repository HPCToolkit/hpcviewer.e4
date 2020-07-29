package edu.rice.cs.hpctraceviewer.ui.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;

import edu.rice.cs.hpctraceviewer.ui.operation.TraceOperation;

public class RedoOperationAction  
{
	protected IUndoableOperation[] getHistory() {
		return TraceOperation.getRedoHistory();
	}


	@Execute
	protected void execute() {

		IUndoableOperation[] undos = TraceOperation.getUndoHistory();

		if (undos.length == 0) {
			// hack: when there's no undo, we need to remove the current
			// history into the undo stack. To do this properly, we
			// should perform an extra redo before the real redo

			//doRedo();
			IUndoableOperation[] operations = getHistory();
			if (operations != null && operations.length-2>=0) {
				execute(operations[operations.length-2]);
				return;
			}
		}
		doRedo();
	}

	
	@CanExecute
	protected boolean canExecute() {
		final IUndoableOperation []ops = getHistory(); 
		boolean status = ops.length > 0;
		System.out.println("can redo: " + status +" , "  + ops);
		return status;
	}
	
	
	/****
	 * helper method to perform the default redo
	 */
	private void doRedo() {
		try {
			IStatus status = TraceOperation.getOperationHistory().
					redo(TraceOperation.undoableContext, null, null);
			if (!status.isOK()) {
				System.err.println("Cannot redo: " + status.getMessage());
			}
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
	

	protected void execute(IUndoableOperation operation) {
		try {
			TraceOperation.getOperationHistory().redoOperation(operation, null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
}
