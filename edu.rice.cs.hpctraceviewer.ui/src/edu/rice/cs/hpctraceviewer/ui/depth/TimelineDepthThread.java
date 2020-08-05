package edu.rice.cs.hpctraceviewer.ui.depth;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;

import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.DataLinePainting;
import edu.rice.cs.hpctraceviewer.data.DataPreparation;
import edu.rice.cs.hpctraceviewer.data.ImageTraceAttributes;
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

	/*****
	 * Thread initialization
	 *  
	 * @param data : global data
	 * @param canvas : depth view canvas
	 * @param scaleX : The scale in the x-direction of pixels to time 
	 * @param scaleY : The scale in the y-direction of max depth
	 * @param width  : the width
	 */
	public TimelineDepthThread( SpaceTimeDataController data, 
								ImageTraceAttributes attributes,
								double scaleY, Queue<TimelineDataSet> queue, 
								AtomicInteger timelineDone, 
								IProgressMonitor monitor)
	{
		super(data, scaleY, queue, timelineDone, monitor);
	}


	@Override
	protected ProcessTimeline getNextTrace(AtomicInteger currentLine) {
		ProcessTimeline depthTrace = stData.getCurrentDepthTrace();
		if (depthTrace == null) {
			monitor.setCanceled(true);
			monitor.done(); // forcing to reset the title bar
			return null;
		}
		final ImageTraceAttributes attributes = stData.getAttributes();
		int currentDepthLineNum = currentLine.getAndIncrement();
		if (currentDepthLineNum < Math.min(attributes.getDepthPixelVertical(), stData.getMaxDepth())) {
			
			// I can't get the data from the ProcessTimeline directly, so create
			// a ProcessTimeline with data=null and then copy the actual data to
			// it.
			ProcessTimeline toDonate = new ProcessTimeline(currentDepthLineNum,
					stData.getScopeMap(), stData.getBaseData(), 
					stData.computeScaledProcess(), attributes.getPixelHorizontal(),
					attributes.getTimeInterval(), 
					stData.getMinBegTime() + attributes.getTimeBegin());

			toDonate.copyDataFrom(depthTrace);

			return toDonate;
		} else
			return null;
	}

	@Override
	protected boolean init(ProcessTimeline trace) {

		return true;
	}

	@Override
	protected void finalize() {
	}

	@Override
	protected DataPreparation getData( DataLinePainting data ) {
		
		// the current depth is the current line to be painted
		
		data.depth = data.ptl.line();
		
		return new DepthDataPreparation(data);
	}	
}
