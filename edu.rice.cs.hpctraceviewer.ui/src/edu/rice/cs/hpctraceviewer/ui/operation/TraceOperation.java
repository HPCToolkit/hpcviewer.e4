package edu.rice.cs.hpctraceviewer.ui.operation;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryFactory;

import edu.rice.cs.hpctraceviewer.data.Frame;


/********************************************
 * 
 * generic trace operation
 *
 ********************************************/
public abstract class TraceOperation extends AbstractOperation 
{
	
	final static public IUndoContext traceContext = new TraceOperationContext();
	final static public IUndoContext undoableContext = new UndoableOperationContext();

	protected Frame frame;
	
	public TraceOperation(String label) {
		this(label,null);
	}
	
	public TraceOperation(String label, Frame frame) {
		super(label + " " + frame);
		addContext(traceContext);
		this.frame = frame;
	}

	public Frame getFrame() {
		return frame;
	}
	
	static public IOperationHistory getOperationHistory() {
		return OperationHistoryFactory.getOperationHistory();
	}
	
	public static IUndoableOperation[] getUndoHistory()
	{
		return getOperationHistory().getUndoHistory(undoableContext);
	}
	
	public static IUndoableOperation[] getRedoHistory()
	{
		return getOperationHistory().getRedoHistory(undoableContext);
	}
	
	public static void clear() 
	{
		TraceOperation.getOperationHistory().
			dispose(undoableContext, true, true, true);
	}
}
