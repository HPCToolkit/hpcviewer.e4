// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;


public class MenuOperationUndo extends AbstractOperationMenu
{
	static final private String ID_MENU_URI       = "bundleclass://edu.rice.cs.hpctraceviewer.ui/" + MenuOperationUndo.class.getName();

	public MenuOperationUndo() {
		super("Undo");
	}


	@Override
	String getContributionURI() {
		return ID_MENU_URI;
	}


	@Override
	IUndoableOperation[] getOperationHistory(ITracePart tracePart, IUndoContext context) {
		return tracePart.getOperationHistory().getUndoHistory(context);
	}


	@Override
	void execute(ITracePart tracePart, IUndoableOperation operation) throws ExecutionException {
		tracePart.getOperationHistory().undoOperation(operation, null, null);		
	}

}