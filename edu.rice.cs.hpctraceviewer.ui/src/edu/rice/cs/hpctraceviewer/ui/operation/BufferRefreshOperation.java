package edu.rice.cs.hpctraceviewer.ui.operation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.ImageData;

import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;

public class BufferRefreshOperation extends AbstractTraceOperation {

	/**image data that describes current image in detail canvas*/
	private final ImageData detailData;

	public BufferRefreshOperation(SpaceTimeDataController data, 
								  ImageData detailData,
								  IUndoContext context ) {
		super(data, "refresh");
		addContext(context);
		this.detailData = detailData;
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

	public ImageData getImageData() {
		return detailData;
	}
}
