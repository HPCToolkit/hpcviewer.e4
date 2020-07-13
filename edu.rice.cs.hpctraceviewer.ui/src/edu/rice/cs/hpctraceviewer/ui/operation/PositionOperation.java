package edu.rice.cs.hpctraceviewer.ui.operation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import edu.rice.cs.hpctraceviewer.data.Position;


/**********************************
 * 
 * Operation for changes of cursor position
 * @see getPosition 
 * 	method to retrieve the current position
 **********************************/
public class PositionOperation extends AbstractOperation 
{
	final static public IUndoContext context = new PositionOperationContext();
	final private Position position;
	
	public PositionOperation(Position position)
	{
		super(PositionOperationContext.label);
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
	
	
	/**********
	 * 
	 * private inner class for the context of this operation
	 *
	 **********/
	static private class PositionOperationContext implements IUndoContext
	{
		private final static String label = "PositionOperationContext";

		@Override
		public String getLabel() {
			return label;
		}

		@Override
		public boolean matches(IUndoContext context) {
			return context.getLabel() == label;
		}
		
	}
}
