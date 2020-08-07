package edu.rice.cs.hpctraceviewer.ui.depth;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.ImageTraceAttributes;
import edu.rice.cs.hpctraceviewer.data.TimelineDataSet;
import edu.rice.cs.hpctraceviewer.ui.base.ISpaceTimeCanvas;
import edu.rice.cs.hpctraceviewer.ui.internal.BasePaintThread;
import edu.rice.cs.hpctraceviewer.ui.internal.BaseTimelineThread;
import edu.rice.cs.hpctraceviewer.ui.internal.BaseViewPaint;
import edu.rice.cs.hpctraceviewer.ui.internal.ImagePosition;


/******************************************************
 * 
 * Painting class for depth view
 *
 ******************************************************/
public class DepthViewPaint extends BaseViewPaint {

	private final GC masterGC;
	private final AtomicInteger timelineDone, numDataCollected;
	private float numPixels;

	public DepthViewPaint(final GC masterGC, SpaceTimeDataController data,
			ImageTraceAttributes attributes, boolean changeBound, ISpaceTimeCanvas canvas, 
			ExecutorService threadExecutor) {
		
		super("Depth view", data, changeBound,  canvas);
		this.masterGC = masterGC;
		timelineDone  = new AtomicInteger(0);
		numDataCollected  = new AtomicInteger(0);
	}

	@Override
	protected boolean startPainting(int linesToPaint, int numThreads, boolean changedBounds) 
	{
		ImageTraceAttributes attributes = controller.getAttributes();
		int process = attributes.getPosition().process;
		
		// we need to check if the data is ready.
		// data is ready iff 
		//  - a process has been selected for the depth view (within the range)
		//  - and the main view has finished generated the timelines
		
		if (process >= attributes.getProcessBegin() && process <= attributes.getProcessEnd()) {
			// TODO warning: data races for accessing the current process timeline 
			if ( controller.getCurrentDepthTrace() != null) {
				numPixels = attributes.getDepthPixelVertical()/(float)controller.getMaxDepth();
				return changedBounds;
			}
		}
		return false;
	}


	@Override
	protected int getNumberOfLines() {
		final ImageTraceAttributes attributes = controller.getAttributes();
		return Math.min(attributes.getDepthPixelVertical(), controller.getMaxDepth());
	}

	@Override
	protected BaseTimelineThread getTimelineThread(ISpaceTimeCanvas canvas, double xscale, double yscale,
			Queue<TimelineDataSet> queue, IProgressMonitor monitor) {
		
		ImageTraceAttributes attributes = controller.getAttributes();
		
		return new TimelineDepthThread( controller, attributes, yscale, queue, numDataCollected,
										monitor);
	}

	@Override
	protected void launchDataGettingThreads(boolean changedBounds,
			int numThreads) {
		//We don't want to get data here.
	}

	@Override
	protected BasePaintThread getPaintThread(Queue<TimelineDataSet> queue, int numLines, 
			   int width, IProgressMonitor monitor) {

		return new DepthPaintThread(controller, queue, numLines, 
									numDataCollected, timelineDone, 
									width, monitor);
	}

	@Override
	protected void drawPainting(ISpaceTimeCanvas canvas, ImagePosition img) {
		if (masterGC != null && !masterGC.isDisposed() && img != null && img.image != null)
		{
			Display.getDefault().syncExec(()-> {
				try {
					masterGC.drawImage(	img.image,  // source image
										0, 0, 		// source X and Y
										img.image.getBounds().width, img.image.getBounds().height,  	// Source width and height
										0, Math.round(img.position * numPixels), 						// target x and y
										img.image.getBounds().width, img.image.getBounds().height);		// target width and height
				} catch (Exception e) {
					e.printStackTrace();
				}
				img.image.dispose();
			});
		}
	}

	@Override
	protected void endPainting(boolean isCanceled) {
		if (masterGC != null && !masterGC.isDisposed())
			masterGC.dispose();
	}
}
