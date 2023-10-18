package edu.rice.cs.hpctraceviewer.ui.depth;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;

import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpcbase.IProcessTimeline;
import edu.rice.cs.hpctraceviewer.data.DataLinePainting;
import edu.rice.cs.hpctraceviewer.data.DataPreparation;
import edu.rice.cs.hpctraceviewer.data.TraceDisplayAttribute;
import edu.rice.cs.hpctraceviewer.data.TimelineDataSet;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimeline;
import edu.rice.cs.hpctraceviewer.ui.internal.BaseTimelineThread;


/*************************************************
 * 
 * Timeline thread for depth view
 *
 *************************************************/
public class TimelineDepthThread 
	extends BaseTimelineThread
{
	private final int visibleDepths;
	
	private final AtomicInteger currentLine;

	/*****
	 * Thread initialization
	 *  
	 * @param data 
	 * 			global data of the trace
	 * @param canvas 
	 * 			depth view canvas
	 * @param queue
	 * 			global queue to store the result data
	 * @param timelineDone
	 * @param monitor
	 * @param visibleDepths
	 */
	public TimelineDepthThread( SpaceTimeDataController data, 
								double scaleY, 
								Queue<TimelineDataSet> queue, 
								AtomicInteger timelineDone, 
								IProgressMonitor monitor,
								int visibleDepths)
	{
		super(data, scaleY, queue, monitor);
		this.visibleDepths = visibleDepths;
		currentLine = timelineDone;
	}


	@Override
	protected IProcessTimeline getNextTrace() {
		var depthTrace = stData.getCurrentSelectedTraceline();
		if (depthTrace == null) {
			monitor.setCanceled(true);
			monitor.done(); // forcing to reset the title bar
			return null;
		}
		final TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
		int currentDepthLineNum = currentLine.getAndIncrement();
		if (currentDepthLineNum < Math.min(attributes.getDepthPixelVertical(), visibleDepths)) {
			
			// I can't get the data from the ProcessTimeline directly, so create
			// a ProcessTimeline with data=null and then copy the actual data to
			// it.
			ProcessTimeline toDonate = new ProcessTimeline(currentDepthLineNum, stData,
														   depthTrace.getProfileIdTuple());

			toDonate.copyDataFrom(depthTrace);

			return toDonate;
		} else {
			return null;
		}
	}

	@Override
	protected boolean init(IProcessTimeline trace) {
		return true;
	}

	@Override
	protected void finalizeTraceCollection() {
		// no op
	}

	@Override
	protected DataPreparation getData( DataLinePainting data ) {
		int selectedDepth = stData.getTraceDisplayAttribute().getDepth();
		int maxDepth = stData.getMaxDepth();
		int minDepth = getMinDepth(selectedDepth, visibleDepths, maxDepth);
		
		// the current depth is the current line to be painted		
		data.depth = minDepth + data.ptl.line();
		return new DepthDataPreparation(data, minDepth, visibleDepths);
	}
	
	
	/***
	 * Retrieve the first visible depth
	 * 
	 * @param currentDepth the current selected depth
	 * @param visibleDepths number of visible depths
	 * @param maxDepth maximum depths
	 * @return the first visible depth
	 */
	static int getMinDepth(int currentDepth, int visibleDepths, int maxDepth) {
		float mid = (float) (visibleDepths * 0.5);
		if (currentDepth>=0 && currentDepth<= mid)
			return 0;
		
		if (currentDepth+mid >= maxDepth)
			return (int) (maxDepth-visibleDepths);
		
		int mx = (int) (currentDepth + mid);
		return mx-visibleDepths;
	}
}
