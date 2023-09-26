package edu.rice.cs.hpctraceviewer.ui.main;


import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;

import edu.rice.cs.hpcdata.db.IdTuple;
//import edu.rice.cs.hpcremote.data.SpaceTimeDataControllerRemote;
import edu.rice.cs.hpctraceviewer.data.DataLinePainting;
import edu.rice.cs.hpctraceviewer.data.DataPreparation;
import edu.rice.cs.hpctraceviewer.data.TraceDisplayAttribute;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.TimelineDataSet;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimeline;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimelineService;
import edu.rice.cs.hpctraceviewer.ui.internal.BaseTimelineThread;


public class TimelineThread 
	extends BaseTimelineThread
{
	private final int totalLines;

	/**Stores whether or not the bounds have been changed*/
	private boolean changedBounds;
	
	/***********************************************************************************************************
	 * Creates a TimelineThread with SpaceTimeData _stData; the rest of the parameters are things for drawing
	 * @param changedBounds - whether or not the thread needs to go get the data for its ProcessTimelines.
	 ***********************************************************************************************************/
	public TimelineThread( SpaceTimeDataController stData, 
						   boolean _changedBounds, double _scaleY, Queue<TimelineDataSet> queue, 
						   AtomicInteger currentLine, int totalLines, IProgressMonitor monitor)
	{
		super(stData, _scaleY, queue, currentLine, monitor);
		
		changedBounds = _changedBounds;		
		this.totalLines	= totalLines;
	}
	
	
	@Override
	protected ProcessTimeline getNextTrace(AtomicInteger currentLine) {
		
		// case 1: remote database
		/*
		if (stData instanceof SpaceTimeDataControllerRemote) {
			return ((SpaceTimeDataControllerRemote)stData).getNextTrace(currentLine, totalLines, 
																		changedBounds, monitor);
		}
		*/
		// case 2: local database
		
		ProcessTimeline timeline = null;
		// retrieve the current processing line, and atomically increment so that 
		// other threads will not increment at the same time
		// if the current line reaches the number of traces to render, we are done
		int currentLineNum = currentLine.getAndIncrement();
		
		if (currentLineNum >= totalLines)
			return null;
		
		ProcessTimelineService traceService = stData.getProcessTimelineService();
		
		if (changedBounds) {
			TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
			IdTuple idtuple;
			try {
				idtuple = getProfileIndexToPaint(currentLineNum, attributes);
			} catch (IOException e) {
				throw new IllegalAccessError(e.getMessage());
			}
			
			ProcessTimeline currentTimeline = new ProcessTimeline(currentLineNum,
					  											  stData, 
																  idtuple);
			
			if (traceService.setProcessTimeline(currentLineNum, currentTimeline)) {
				timeline = currentTimeline;
			} else {
				monitor.setCanceled(true);
				monitor.done();
			}
		} else {
			timeline = traceService.getProcessTimeline(currentLineNum);
		}
		return timeline;
	}

	
	@Override
	protected boolean init(ProcessTimeline trace) throws IOException {
		//nextTrace.data is not empty if the data is from the server
		if(changedBounds)
		{
			if (trace.isEmpty()) {
				
				trace.readInData();

				ProcessTimelineService traceService = stData.getProcessTimelineService();
				if (!traceService.setProcessTimeline(trace.line(), trace)) {
					// something wrong happens, perhaps data races ?
					monitor.setCanceled(true);
					monitor.done();
					return false;
				}
			}
			trace.shiftTimeBy(stData.getMinBegTime());
		}
		return (trace.size()>=2);
	}

	@Override
	protected void finalizeTraceCollection() {
		// no op
	}

	@Override
	protected DataPreparation getData(DataLinePainting data) {
		return new DetailDataPreparation(data);
	}

	
	/** Returns the index of the file to which the line-th line corresponds. 
	 * @throws IOException */

	private IdTuple getProfileIndexToPaint(int line, TraceDisplayAttribute attributes) throws IOException {
		var listIdTuples = stData.getExperiment().getThreadData().getIdTuples();
		
		int numTimelinesToPaint = attributes.getProcessInterval();
		int index = 0;
		
		if (numTimelinesToPaint > attributes.getPixelVertical()) {
			index = attributes.getProcessBegin() + (line * numTimelinesToPaint)
					/ (attributes.getPixelVertical());
		} else {
			index = attributes.getProcessBegin() + line;
		}
		return listIdTuples.get(index);
	}
}