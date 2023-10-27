package edu.rice.cs.hpctraceviewer.ui.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;

import org.eclipse.core.runtime.IProgressMonitor;

import edu.rice.cs.hpcbase.IProcessTimeline;
import edu.rice.cs.hpctraceviewer.config.TracePreferenceManager;
import edu.rice.cs.hpctraceviewer.data.DataLinePainting;
import edu.rice.cs.hpctraceviewer.data.DataPreparation;
import edu.rice.cs.hpctraceviewer.data.TraceDisplayAttribute;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.TimelineDataSet;



/*****************************************************************************
 * 
 * Abstract class for handling data collection
 * The class is based on Java Callable, and returns the number of
 * traces the thread has been processed
 *
 * @author Michael Franco 
 * 	modified by Laksono and Philip
 *****************************************************************************/
public abstract class BaseTimelineThread implements Callable<Integer> {

	/**The minimum height the samples need to be in order to paint the white separator lines.*/
	public static final byte MIN_HEIGHT_FOR_SEPARATOR_LINES = 15;

	/**The SpaceTimeData that this thread gets its files from and adds it data and images to.*/
	protected final SpaceTimeDataController stData;
	
	/**The scale in the y-direction of pixels to processors (for the drawing of the images).*/
	private final double scaleY;	
	private final Queue<TimelineDataSet> queue;
	protected final IProgressMonitor monitor;
	protected final Map<Integer, List<?>> mapInvalidData;


	protected BaseTimelineThread( SpaceTimeDataController stData,
							   double scaleY, 
							   Queue<TimelineDataSet> queue, 
							   IProgressMonitor monitor)
	{
		this.stData 	   = stData;
		this.scaleY 	   = scaleY;
		this.queue 		   = queue;
		this.monitor 	   = monitor;
		
		mapInvalidData = new HashMap<>();
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	public Integer call() throws Exception {

		var trace = getNextTrace();
		TraceDisplayAttribute attributes = stData.getTraceDisplayAttribute();
		
		final double pixelLength = (attributes.getTimeInterval())/(double)attributes.getPixelHorizontal();
		final long timeBegin = attributes.getTimeBegin();
		int num_invalid_samples = 0;
		
		DataLinePainting data = new DataLinePainting();
		
		data.colorTable    = stData.getColorTable();
		data.usingMidpoint = TracePreferenceManager.isMidpointEnabled();
		data.pixelLength   = pixelLength;
		data.begTime       = timeBegin;
		data.depth		   = stData.getTraceDisplayAttribute().getDepth();
		
		while (trace != null)
		{
			// ---------------------------------
			// begin collecting the data if needed
			// ---------------------------------
			if (init(trace))
			{	
				int imageHeight = getRenderingHeight(scaleY, trace.line(), trace.line()+1);
				
    			// ---------------------------------
    			// do the data preparation
    			// ---------------------------------
				data.ptl    = trace;
				data.height = imageHeight;

				final DataPreparation dataTo = getData(data);
				
				int num = dataTo.collect();
				if (num > 0) {
					num_invalid_samples += num;
					List<Integer> listInvalid = dataTo.getInvalidData();
					int proc = trace.getProfileIdTuple().getProfileIndex()-1;
					mapInvalidData.put(proc, listInvalid);
				}
				
				final TimelineDataSet dataSet = dataTo.getList();
				queue.add(dataSet);
			} else {
				// empty trace, we need to notify the BasePaintThread class
				// of this anomaly by adding NullTimeline
				queue.add(TimelineDataSet.NULLTimeline);
			}
			if (monitor.isCanceled())
				return null;
			
			monitor.worked(1);
			
			trace = getNextTrace();
			
			// ---------------------------------
			// finalize
			// ---------------------------------
			finalizeTraceCollection();
		}
		return Integer.valueOf(num_invalid_samples);
	}
	

	public Map<Integer, List<?>> getInvalidData() {
		return mapInvalidData;
	}
	
	/***
	 * compute the height of a given line
	 *  
	 * @param scaleY vertical scale
	 * @param line1  the line number of process 1
	 * @param line2  the line number of process 2
	 * 
	 * @return the height of process 1
	 */
	public static int getRenderingHeight(double scaleY, int line1, int line2) {
		
		// this height computation causes inconsistency between different lines
		// we need to find a better way, more deterministic and consistent
		int h1 = (int) Math.round(scaleY*line1);
		int h2 = (int) Math.round(scaleY*line2);			
		int imageHeight = h2 - h1;
		
		if (scaleY > MIN_HEIGHT_FOR_SEPARATOR_LINES)
			imageHeight--;
		else
			imageHeight++;

		return imageHeight;
	}
	
	/****
	 * Retrieve the next available trace, null if no more trace available 
	 * 
	 * @return
	 ****/
	protected abstract IProcessTimeline getNextTrace();
	
	protected abstract boolean init(IProcessTimeline trace) throws IOException;
	
	protected abstract void finalizeTraceCollection();
	
	protected abstract DataPreparation getData( DataLinePainting data);
}
