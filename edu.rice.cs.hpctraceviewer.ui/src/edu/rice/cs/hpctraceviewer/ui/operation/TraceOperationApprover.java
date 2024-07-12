// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.operation;

import org.eclipse.core.commands.operations.IOperationApprover;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class TraceOperationApprover implements IOperationApprover {

	@Override
	public IStatus proceedRedoing(IUndoableOperation operation,
			IOperationHistory history, IAdaptable info) {
		return Status.OK_STATUS;
	}

	@Override
	public IStatus proceedUndoing(IUndoableOperation operation,
			IOperationHistory history, IAdaptable info) {
		return Status.OK_STATUS;
	}

}
