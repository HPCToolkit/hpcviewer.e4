// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.main;


import java.io.IOException;
import java.util.Queue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcbase.IProcessTimeline;
import edu.rice.cs.hpctraceviewer.data.DataLinePainting;
import edu.rice.cs.hpctraceviewer.data.DataPreparation;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.TimelineDataSet;
import edu.rice.cs.hpctraceviewer.ui.internal.BaseTimelineThread;


public class TimelineThread 
	extends BaseTimelineThread
{
	/**Stores whether or not the bounds have been changed*/
	private boolean changedBounds;
	
	/***********************************************************************************************************
	 * Creates a TimelineThread with SpaceTimeData _stData; the rest of the parameters are things for drawing
	 * @param changedBounds - whether or not the thread needs to go get the data for its ProcessTimelines.
	 ***********************************************************************************************************/
	public TimelineThread( SpaceTimeDataController stData, 
						   boolean _changedBounds, 
						   double _scaleY, 
						   Queue<TimelineDataSet> queue, 
						   IProgressMonitor monitor)
	{
		super(stData, _scaleY, queue, monitor);
		
		changedBounds = _changedBounds;
	}
	
	
	@Override
	protected IProcessTimeline getNextTrace() {
		try {
			return stData.getNextTrace();
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass()).error("Unable to get the next trace", e);
		}
		return null;
	}

	
	@Override
	protected boolean init(IProcessTimeline trace) throws IOException {
		//nextTrace.data is not empty if the data is from the server
		if(changedBounds)
		{
			if (trace.isEmpty()) {
				trace.readInData();
			}
			if (!stData.setTraceline(trace.line(), trace)) {
				// something wrong happens, perhaps data races ?
				monitor.setCanceled(true);
				monitor.done();
				return false;
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
}