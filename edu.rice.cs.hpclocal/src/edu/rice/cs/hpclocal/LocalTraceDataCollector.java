// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpclocal;

import java.io.IOException;
import edu.rice.cs.hpcbase.AbstractTraceDataCollector;
import edu.rice.cs.hpcbase.IFilteredData;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.version4.DataRecord;
import edu.rice.cs.hpcdata.util.Constants;


/***********************************************************
 * 
 * Main class to access trace data. All classes that requires
 * to access trace data has to instantiate this class.<br/>
 * 
 * For historical purpose, this class manages data per rank
 * since the first version of data is one file for each rank
 * 
 ***********************************************************/
public class LocalTraceDataCollector extends AbstractTraceDataCollector 
{
	//	tallent: safe to assume version 1.01 and greater here
	public static final int HeaderSzMin = Header.MagicLen + Header.VersionLen + Header.EndianLen + Header.FlagsLen;
	
	public static final float NUM_PIXELS_TOLERATED = 1.0f;
	
	private final IdTuple profile;
	
	/** 
	 * These must be initialized in local mode. 
	 * They should be considered final unless the data is remote.*/
	private AbstractBaseData   data;
	
	/***
	 * Create a new instance of trace data for a given rank of process or thread 
	 * Used only for local
	 * @param iFilteredData
	 * 			Access to the local trace data file
	 * @param profile
	 * 			The execution context. It can be a process, thread, or GPU context
	 * @param widthInPixels
	 * 			The number of horizontal samples or pixels
	 * @param option
	 * 			The trace option. This can be whether to reveal small traces or not
	 * 
	 * @see TraceOption
	 */
	public LocalTraceDataCollector(IFilteredData iFilteredData, IdTuple profile, int widthInPixels, TraceOption option)
	{
		super(option, widthInPixels);
		
		this.profile = profile;
		
		//:'( This is a safe cast because this constructor is only
		//called in local mode but it's so ugly....
		data = (AbstractBaseData) iFilteredData;
	}

	
	/***
	 * Reading data from file. This method has to be called FIRST before calling other APIs.
	 * @apiNote This is a hack. If possible, call this immediately after the constructor.
	 * 
	 * @param timeStart
	 * @param timeRange
	 * @param pixelLength 
	 * 			the range of time per pixel. Its unit is time, usually nanoseconds for data version 4.
	 * @throws IOException 
	 */
	@Override
	public void readInData(long timeStart, long timeRange, double pixelLength) throws IOException
	{
		long minloc = data.getMinLoc(profile);
		long maxloc = data.getMaxLoc(profile);
		
		// corner case: empty samples
		if (minloc >= maxloc) 
			return;
		
		// get the start location
		final long startLoc = this.findTimeInInterval(timeStart, minloc, maxloc);
		
		// get the end location
		final long endTime = timeStart + timeRange;
		final long endLoc = Math.min(this.findTimeInInterval(endTime, minloc, maxloc) + data.getRecordSize(), maxloc );

		// get the number of records data to display
		final long numRec = 1+this.getNumberOfRecords(startLoc, endLoc);
		
		var numPixelH = getNumPixelHorizontal();
		
		// --------------------------------------------------------------------------------------------------
		// if the data-to-display is fit in the display zone, we don't need to use recursive binary search
		//	we just simply display everything from the file
		// --------------------------------------------------------------------------------------------------
		if (numRec<=numPixelH) {			
			// display all the records
			// increment one record of data contains of an integer (cpid) and a long (time)
			for(long i=startLoc;i<=endLoc; i+=data.getRecordSize()) {
				addSampleToLastIndex(getData(i));
			}			
		} else {
			// the data is too big: try to fit the "big" data into the display			
			//fills in the rest of the data for this process timeline
			this.sampleTimeLine(startLoc, endLoc, 0, numPixelH, 0, pixelLength, timeStart);			
		}
		
		// --------------------------------------------------------------------------------------------------
		// get the last data if necessary: the rightmost time is still less then the upper limit
		// 	I think we can add the rightmost data into the list of samples
		// --------------------------------------------------------------------------------------------------
		if (endLoc <= maxloc) {
			final DataRecord dataLast = this.getData(endLoc);
			addSampleToLastIndex(dataLast);
		}
		
		// --------------------------------------------------------------------------------------------------
		// get the first data if necessary: the leftmost time is still bigger than the lower limit
		//	similarly, we add to the list 
		// --------------------------------------------------------------------------------------------------
		if ( startLoc > minloc ) {
			final DataRecord dataFirst = this.getData(startLoc - data.getRecordSize());
			this.addSample(0, dataFirst);
		}
		postProcess();
	}

	
	/*******************************************************************************************
	 * Recursive method that fills in times and timeLine with the correct data from the file.
	 * Takes in two pixel locations as endpoints and finds the timestamp that owns the pixel
	 * in between these two. It then recursively calls itself twice - once with the beginning 
	 * location and the newfound location as endpoints and once with the newfound location 
	 * and the end location as endpoints. Effectively updates times and timeLine by calculating 
	 * the index in which to insert the next data. This way, it keeps times and timeLine sorted.
	 * 
	 * @author Reed Landrum and Michael Franco
	 * 
	 * @param minLoc The beginning location in the file to bound the search.
	 * @param maxLoc The end location in the file to bound the search.
	 * @param startPixel The beginning pixel in the image that corresponds to minLoc.
	 * @param endPixel The end pixel in the image that corresponds to maxLoc.
	 * @param minIndex An index used for calculating the index in which the data is to be inserted.
	 * @param pixelLength the range of time per pixel. Its unit is time, usually nanoseconds for data version 4.
	 * @param startingTime starting time
	 * 
	 * @return Returns the index that shows the size of the recursive subtree that has been read.
	 * Used for calculating the index in which the data is to be inserted.
	 * @throws IOException 
	 ******************************************************************************************/
	private int sampleTimeLine(long minLoc, long maxLoc, int startPixel, int endPixel, int minIndex, 
			double pixelLength, long startingTime) throws IOException
	{
		int midPixel = (startPixel+endPixel)/2;
		if (midPixel == startPixel)
			return 0;
		
		long loc = findTimeInInterval((long)(midPixel*pixelLength)+startingTime, minLoc, maxLoc);
		
		final DataRecord nextData = this.getData(loc);
		
		// corner case: if we are forced to reveal the gpu traces (since they are short-lives),
		// we need to check if the current sample is an "idle" activity or not.
		// if this is the case, we look at the right and if it isn't idle, we'll use
		// this sample instead.
		if (getTraceOption() == TraceOption.REVEAL_GPU_TRACE && isIdle(nextData.cpId)) {	
			// if this sample is idle, check if the next sample is also idle or not
			// (mostly not idle). If the next sample is NOT idle and within a range
			// let's move to the next sample instead
			long rightLoc = loc + data.getRecordSize();
			final var rightData = getData(rightLoc);
			if (!isIdle(rightData.cpId)) {

				// make sure the next non-idle samples are within the tolerated number of pixels
				// if the next sample is too far, we just skip it and accept the "idle" sample.
				var dTime = (rightData.timestamp - nextData.timestamp) / pixelLength < NUM_PIXELS_TOLERATED;
				if (dTime) {
					loc = rightLoc;
					nextData.cpId = rightData.cpId;
					nextData.timestamp = rightData.timestamp;
				}
			}

		}
		
		addSample(minIndex, nextData);
		
		int addedLeft  = sampleTimeLine(minLoc, loc, startPixel, midPixel, minIndex, pixelLength, startingTime);
		int addedRight = sampleTimeLine(loc, maxLoc, midPixel, endPixel, minIndex+addedLeft+1, pixelLength, startingTime);
		
		return addedLeft+addedRight+1;
	}
	
	/*********************************************************************************
	 *	Returns the location in the traceFile of the trace data (time stamp and cpid)
	 *	Precondition: the location of the trace data is between minLoc and maxLoc.
	 * @param time 
	 * 			the time to be found
	 * @param left_boundary_offset 
	 * 			the start location. 0 means the beginning of the data in a process
	 * @param right_boundary_offset 
	 * 			the end location.
	 * @throws IOException 
	 ********************************************************************************/
	private long findTimeInInterval(long time, long left_boundary_offset, long right_boundary_offset) throws IOException
	{
		if (left_boundary_offset == right_boundary_offset) return left_boundary_offset;

		long left_index = getRelativeLocation(left_boundary_offset);
		long right_index = getRelativeLocation(right_boundary_offset);
		
		long left_time  = data.getLong(left_boundary_offset);
		long right_time = data.getLong(right_boundary_offset);
		
		// apply "Newton's method" to find target time
		while (right_index - left_index > 1) {
			long predicted_index;
			final long time_range = right_time - left_time;
			final double rate = (double)time_range / (right_index - left_index);
			final long mtime  = time_range / 2;
			if (time <= mtime) {
				predicted_index = Math.max((long) ((time - left_time) / rate) + left_index, left_index);
			} else {
				predicted_index = Math.min(right_index - (long) ((right_time - time) / rate), right_index); 
			}
			
			// adjust predicted_index so that it differs from the endpoints.
			// without that, the search may fail to converge.
			if (predicted_index <= left_index) 
				predicted_index = left_index + 1;

			if (predicted_index >= right_index)
				predicted_index = right_index - 1;

			long temp = data.getLong(getAbsoluteLocation(predicted_index));

			if (time >= temp) {
				left_index = predicted_index;
				left_time = temp;
			} else {
				right_index = predicted_index;
				right_time = temp;
			}
		}

		return getAbsoluteLocation(left_index);
	}
	
	private long getAbsoluteLocation(long relativePosition)
	{
		return data.getMinLoc(profile) + (relativePosition * data.getRecordSize());
	}
	
	private long getRelativeLocation(long absolutePosition)
	{
		return (absolutePosition-data.getMinLoc(profile)) / data.getRecordSize();
	}

	
	private DataRecord getData(long location) throws IOException
	{
		final long time = data.getLong(location);
		final int cpId = data.getInt(location + Constants.SIZEOF_LONG);
		int metricId = edu.rice.cs.hpctraceviewer.data.util.Constants.dataIdxNULL;
		
		return new DataRecord(time, cpId, metricId);
	}
	
	private long getNumberOfRecords(long start, long end)
	{
		return (end-start) / (data.getRecordSize());
	}


	@Override
	public void dispose() {
		if (data != null)
			data.dispose();
		
		data = null;

		super.dispose();
	}
}
