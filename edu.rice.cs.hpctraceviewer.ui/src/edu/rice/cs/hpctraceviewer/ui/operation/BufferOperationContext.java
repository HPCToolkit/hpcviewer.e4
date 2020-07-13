package edu.rice.cs.hpctraceviewer.ui.operation;

import org.eclipse.core.commands.operations.IUndoContext;

public class BufferOperationContext implements IUndoContext 
{
	private final static String label = "BufferOperationContext";
	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public boolean matches(IUndoContext context) {
		return context.getLabel()==label;
	}

}
