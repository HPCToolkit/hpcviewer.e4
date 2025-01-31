// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.depth;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.GC;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.TraceDisplayAttribute;
import edu.rice.cs.hpctraceviewer.data.TimelineDataSet;
import edu.rice.cs.hpctraceviewer.ui.base.ISpaceTimeCanvas;
import edu.rice.cs.hpctraceviewer.ui.internal.BasePaintThread;
import edu.rice.cs.hpctraceviewer.ui.internal.BaseTimelineThread;
import edu.rice.cs.hpctraceviewer.ui.internal.BaseViewPaint;
import edu.rice.cs.hpctraceviewer.ui.internal.ImagePosition;


/******************************************************
 * 
 * Painting class for depth view.
 * 
 * Instantiate this class every time we want to paint the depth view
 * Even though there is no new data needs to be fetched
 *
 ******************************************************/
public class DepthViewPaint extends BaseViewPaint 
{
	private final GC masterGC;
	private final AtomicInteger timelineDone;
	private final AtomicInteger numDataCollected;
	private float numPixels;
	private final int visibleDepth;

	
	/**
	 * Create a painting job to replace the current depth view with a 
	 * new paint whether with a new data or not.
	 *  
	 * @param masterGC
	 * 			The graphic context of the depth view
	 * @param data
	 * 			The trace data
	 * @param changeBound
	 * 			Flag whether it requires to fetch new data
	 * @param canvas
	 * 			The canvas to be painted
	 * @param visibleDepth
	 * 			The vertical depth (in pixels)
	 */
	public DepthViewPaint(
			final GC masterGC, 
			SpaceTimeDataController data,
			boolean changeBound, 
			ISpaceTimeCanvas canvas, 
			int visibleDepth) {
		
		super("Depth view", data, changeBound,  canvas);
		this.masterGC = masterGC;
		timelineDone  = new AtomicInteger(0);
		numDataCollected  = new AtomicInteger(0);
		this.visibleDepth = visibleDepth;
	}

	@Override
	protected boolean startPainting(int linesToPaint, int numThreads, boolean changedBounds) 
	{
		TraceDisplayAttribute attributes = controller.getTraceDisplayAttribute();
		int process = attributes.getPosition().process;
		
		// we need to check if the data is ready.
		// data is ready iff 
		//  - a process has been selected for the depth view (within the range)
		//  - and the main view has finished generated the timelines
		
		if (process >= attributes.getProcessBegin() && 
			process <= attributes.getProcessEnd()   &&  
		   (controller.getCurrentSelectedTraceline() != null)) {
				numPixels = attributes.getDepthPixelVertical()/(float)visibleDepth;
				return changedBounds;
			
		}
		return false;
	}


	@Override
	public int getNumberOfLines() {
		final TraceDisplayAttribute attributes = controller.getTraceDisplayAttribute();
		return Math.min(attributes.getDepthPixelVertical(), visibleDepth);
	}

	@Override
	protected BaseTimelineThread getTimelineThread(ISpaceTimeCanvas canvas, double xscale, double yscale,
			Queue<TimelineDataSet> queue, IProgressMonitor monitor) {
		
		return new TimelineDepthThread( controller, yscale, queue, numDataCollected,
										monitor, visibleDepth);
	}

	@Override
	protected void launchDataGettingThreads(boolean changedBounds,
			int numThreads) {
		//We don't want to get data here.
	}

	@Override
	protected BasePaintThread getPaintThread(Queue<TimelineDataSet> queue, int numLines, 
			   int width, IProgressMonitor monitor) {

		return new DepthPaintThread(controller,  queue,  numLines, 
									timelineDone, width, monitor);
	}

	@Override
	protected void drawPainting(ISpaceTimeCanvas canvas, ImagePosition img) {
		if (masterGC != null && !masterGC.isDisposed() && img != null && img.image != null)
		{
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
		}
	}

	@Override
	protected void endPainting(boolean isCanceled) {
		if (masterGC != null && !masterGC.isDisposed())
			masterGC.dispose();
	}

	@Override
	protected void endPreparationThread(BaseTimelineThread thread, int result) { /* no op */ }

	@Override
	protected void endDataPreparation(int numInvalidData) { /* no op */ }
}
