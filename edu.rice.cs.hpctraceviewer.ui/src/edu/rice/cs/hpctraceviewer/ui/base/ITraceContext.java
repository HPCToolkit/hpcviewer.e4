package edu.rice.cs.hpctraceviewer.ui.base;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;

public interface ITraceContext 
{
	public IUndoContext getContext(final String label);
	public IOperationHistory getOperationHistory();
}
