package edu.rice.cs.hpctraceviewer.ui.operation;

import org.eclipse.core.commands.operations.IUndoContext;

/***********************************************************************
 * 
 * Operation context for any undoable operations
 * 
 * $Id: UndoableOperationContext.java 1557 2013-01-02 21:12:28Z laksono $
 *
 ***********************************************************************/

public class UndoableOperationContext implements IUndoContext {

	private final static String label = "UndoableOperationContext";

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public boolean matches(IUndoContext context) {
		return context.getLabel() == label;
	}

}
