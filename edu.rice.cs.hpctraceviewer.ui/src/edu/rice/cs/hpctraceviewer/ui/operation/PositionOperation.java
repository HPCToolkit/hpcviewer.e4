// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.operation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import edu.rice.cs.hpctraceviewer.data.Position;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;


/**********************************
 * 
 * Operation for changes of cursor position
 * @see getPosition 
 * 	method to retrieve the current position
 **********************************/
public class PositionOperation extends AbstractTraceOperation 
{
	private final static String label = "PositionOperationContext";

	final private Position position;
	
	public PositionOperation(SpaceTimeDataController data, Position position, IUndoContext context)
	{
		super(data, label);
		addContext(context);
		
		this.position = position;
	}
	
	public Position getPosition() {
		return position;
	}


	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return Status.OK_STATUS;
	}
}
