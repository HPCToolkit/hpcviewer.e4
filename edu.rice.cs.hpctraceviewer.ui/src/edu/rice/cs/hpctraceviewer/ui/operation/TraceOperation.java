// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.operation;


import org.eclipse.core.commands.operations.IUndoContext;
import edu.rice.cs.hpctraceviewer.data.Frame;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;


/********************************************
 * 
 * generic trace operation
 *
 ********************************************/
public abstract class TraceOperation extends AbstractTraceOperation 
{
	
	protected Frame frame;
	
	public TraceOperation(SpaceTimeDataController data, String label, IUndoContext context) {
		this(data, label, null, context);
	}
	
	public TraceOperation(SpaceTimeDataController data, String label, Frame frame, IUndoContext context) {
		super(data, label + " " + frame);

		this.frame = frame;
		addContext(context);
	}

	public Frame getFrame() {
		return frame;
	}
	
	@Override
	public void dispose() {
		frame = null;
	}
}
