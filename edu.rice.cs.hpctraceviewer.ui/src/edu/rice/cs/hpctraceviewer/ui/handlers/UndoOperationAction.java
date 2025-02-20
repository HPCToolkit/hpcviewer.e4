// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.context.BaseTraceContext;


/***********************************************************************
 * 
 * Class to manage undo operations such as region change, depth change
 * 	or processes pattern change
 * 
 * $Id: UndoOperationAction.java 1556 2013-01-02 21:11:12Z laksono $
 *
 ***********************************************************************/
public class UndoOperationAction
{

	@Execute
	protected void execute(MPart part) {

		ITracePart tracePart = (ITracePart) part.getObject();
		IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_TRACE);
		IUndoableOperation[] operations = tracePart.getOperationHistory().getUndoHistory(context);
		final int len = operations.length;
		if (len<1)
			return;
		
		IUndoableOperation[] redos = tracePart.getOperationHistory().getRedoHistory(context);
		
		if (redos.length == 0 &&  (len-2>=0)) {
			execute(tracePart.getOperationHistory(), operations[len-2]);
			return;
			
		}
		doUndo(tracePart.getOperationHistory(), context);
	}
	
	
	@CanExecute
	protected boolean canExecute(MPart part) {
		ITracePart tracePart = (ITracePart) part.getObject();
		IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_TRACE);
		final IUndoableOperation []ops = tracePart.getOperationHistory().getUndoHistory(context);
		
		return (ops != null) && (ops.length>0);
	}

	/***
	 * helper method to perform the default undo
	 */
	private void doUndo(IOperationHistory opHistory, IUndoContext context ) {

		try {
			IStatus status = opHistory.undo(context, null, null);
			if (!status.isOK()) {
				LoggerFactory.getLogger(getClass()).error("Cannot undo: {}", status.getMessage());
			}
		} catch (ExecutionException e) {
			LoggerFactory.getLogger(getClass()).error("Fail to undo an operation", e);
		}
	}
	

	protected void execute(IOperationHistory opHistory, IUndoableOperation operation) {
		try {
			opHistory.undoOperation(operation, null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
}
