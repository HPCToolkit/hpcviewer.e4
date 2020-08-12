 
package edu.rice.cs.hpctraceviewer.ui.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;

public class MenuOperationRedo extends AbstractOperationMenu
{
	public MenuOperationRedo() {
		super("Redo");
	}


	static final private String ID_MENU_URI = "bundleclass://edu.rice.cs.hpctraceviewer.ui/" + MenuOperationRedo.class.getName();



	@Override
	String getContributionURI() {
		return ID_MENU_URI;
	}


	@Override
	IUndoableOperation[] getOperationHistory(ITracePart tracePart, IUndoContext context) {

		return tracePart.getOperationHistory().getRedoHistory(context);
	}


	@Override
	void execute(ITracePart tracePart, IUndoableOperation operation) throws ExecutionException {
		tracePart.getOperationHistory().redoOperation(operation, null, null);		
	}

}