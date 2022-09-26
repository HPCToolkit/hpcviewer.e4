package edu.rice.cs.hpctraceviewer.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import edu.rice.cs.hpcdata.db.version4.DataRecord;
import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpctraceviewer.config.TracePreferenceManager;
import edu.rice.cs.hpctraceviewer.data.version2.AbstractBaseData;

/***********************************************************
 * 
 * Main class to access trace data. All classes that requires
 * to access trace data has to instantiate this class.<br/>
 * 
 * For historical purpose, this class manages data per rank
 * since the first version of data is one file for each rank
 * 
 ***********************************************************/
public class TraceDataByRank implements ITraceDataCollector 
{
	//	tallent: safe to assume version 1.01 and greater here
	public static final int HeaderSzMin = Header.MagicLen + Header.VersionLen + Header.EndianLen + Header.FlagsLen;
	public static final int RecordSzMin = Constants.SIZEOF_LONG // time stamp
										+ Constants.SIZEOF_INT; // call path id
	
	public static final float NUM_PIXELS_TOLERATED = 1.0f;

	private final TraceOption option;

	/** 
	 * These must be initialized in local mode. 
	 * They should be considered final unless the data is remote.*/
	private final AbstractBaseData   data;
	
	private final int numPixelH;
	private final int rank;
	
	private List<DataRecord> listcpid;
	
	/***
	 * Create a new instance of trace data for a given rank of process or thread 
	 * Used only for local
	 * @param dataAccess
	 * @param currentRank
	 * @param widthInPixels
	 */
	public TraceDataByRank(AbstractBaseData dataAccess, int currentRank, int widthInPixels)
	{
		//:'( This is a safe cast because this constructor is only
		//called in local mode but it's so ugly....
		data = dataAccess;
		rank = currentRank;
		numPixelH = widthInPixels;
		
		option = isGPU() && TracePreferenceManager.getGPUTraceExposure() ?
				  	ITraceDataCollector.TraceOption.REVEAL_GPU_TRACE :
					ITraceDataCollector.TraceOption.ORIGINAL_TRACE;
		
		listcpid = new ArrayList<>(numPixelH);
	}

	
	/***
	 * Special constructor for remote database
	 * @param data
	 * @param rank
	 */
	public TraceDataByRank(DataRecord[] data, int rank) {
		listcpid = new Vector<>(Arrays.asList(data));
		this.rank = rank;
		
		this.data = null;// unused
		numPixelH = 0;	 // unused
		
		option = isGPU() && TracePreferenceManager.getGPUTraceExposure() ?
				  	ITraceDataCollector.TraceOption.REVEAL_GPU_TRACE :
					ITraceDataCollector.TraceOption.ORIGINAL_TRACE;
	}
	
	@Override
	public boolean isEmpty() {
		return listcpid == null || listcpid.isEmpty();
	}

	@Override
	public boolean isGPU() {
		return data.isGPU(rank);
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
		long minloc = data.getMinLoc(rank);
		long maxloc = data.getMaxLoc(rank);
		
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
		
		// --------------------------------------------------------------------------------------------------
		// if the data-to-display is fit in the display zone, we don't need to use recursive binary search
		//	we just simply display everything from the file
		// --------------------------------------------------------------------------------------------------
		if (numRec<=numPixelH) {			
			// display all the records
			// increment one record of data contains of an integer (cpid) and a long (time)
			for(long i=startLoc;i<=endLoc; i+=data.getRecordSize()) {
				listcpid.add(getData(i));
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
		if (endLoc < maxloc) {
			final DataRecord dataLast = this.getData(endLoc);
			this.addSample(listcpid.size(), dataLast);
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
	
	
	/**
	 * Gets the time that corresponds to the index sample in times.
	 * @param sample 
	 * 			the index sample
	 * */
	@Override
	public long getTime(int sample)
	{
		if(sample<0 || listcpid == null || listcpid.isEmpty())
			return 0;

		final int last_index = listcpid.size() - 1;
		if(sample>last_index) {
			// laks 2015.05.19 : I think we should throw exception here 
			// 					 instead of forcing ourself to give a result
			return listcpid.get(last_index-1).timestamp;
		}
		return listcpid.get(sample).timestamp;
	}
	
	/**Gets the cpid that corresponds to the index sample in timeLine.
	 * @param sample the sample index (has to be bigger or equal to zero)
	 * @return call path ID if the sample exist, <0 otherwise
	 * */
	@Override
	public int getCpid(int sample)
	{
		if (sample < listcpid.size())
			return listcpid.get(sample).cpId;
		return -1;
	}

	
	/**Shifts all the times in the ProcessTimeline to the left by lowestStartingTime.*/
	@Override
	public void shiftTimeBy(long lowestStartingTime)
	{
		for(int i = 0; i<listcpid.size(); i++)
		{
			DataRecord timecpid = listcpid.get(i);
			timecpid.timestamp = timecpid.timestamp - lowestStartingTime;
			listcpid.set(i,timecpid);
		}
	}
	
	
	/**Returns the number of elements in this ProcessTimeline.*/
	public int size()
	{
		return listcpid.size();
	}

	
	/**Finds the sample to which 'time' most closely corresponds in the ProcessTimeline.
	 * @param time  the requested time
	 * @param usingMidpoint 
	 * @return the index of the sample if the time is within the range, -1 otherwise 
	 * */
	@Override
	public int findClosestSample(long time, boolean usingMidpoint) throws Exception
	{
		if (listcpid.isEmpty())
			return 0;

		int low = 0;
		int high = listcpid.size() - 1;
		
		long timeMin = listcpid.get(low).timestamp;
		long timeMax = listcpid.get(high).timestamp;
		
		// do not search the sample if the time is out of range
		if (time<timeMin  || time>timeMax) 
			return -1;
		
		int mid = ( low + high ) / 2;
		
		while( low != mid )
		{
			final long time_current = (usingMidpoint ? getTimeMidPoint(mid,mid+1) : listcpid.get(mid).timestamp);
			
			if (time > time_current)
				low = mid;
			else
				high = mid;
			mid = ( low + high ) / 2;
			
		}
		if (usingMidpoint)
		{
			if (time >= getTimeMidPoint(low,low+1))
				return low+1;
			else
				return low;
		}
		// for gpu stream, if we need to force to reveal the gpu trace
		// if the current sample is an idle activity, 
		// we check if the next sample is within the range or not.
		var l = listcpid.get(low);
		if (option == TraceOption.REVEAL_GPU_TRACE && isIdle(l.cpId) && low < listcpid.size()) {			
			var r = listcpid.get(low+1);
			var last  = listcpid.get(listcpid.size()-1);
			var first = listcpid.get(0);
			var timePerPixel = (last.timestamp - first.timestamp)/numPixelH;
			var dtime = (r.timestamp - l.timestamp);
			float dFraction = (float) dtime/timePerPixel;
			
			if (dFraction < NUM_PIXELS_TOLERATED && !isIdle(r.cpId))
				return low+1;
		}
		// without using midpoint, we adopt the leftmost sample approach.
		// this means whoever on the left side, it will be the painted
		return low;
	}

	/***
	 * duplicate data from other object
	 * 
	 * @param traceData: another object to be copied
	 */
	@Override
	public void duplicate(ITraceDataCollector traceData)
	{
		this.listcpid = ((TraceDataByRank)traceData).listcpid;
	}
	
	
	private long getTimeMidPoint(int left, int right) {
		return (listcpid.get(left).timestamp + listcpid.get(right).timestamp) / 2;
	}
	
	
	/***
	 * Check if the call-path id or context id is an idle activity.<br/>
	 * On meta-db, the idle activity is represented by number zero.
	 * On experiment.xml it can be different for each database.
	 * 
	 * @param contextId
	 * 			The call-path id or context id.
	 * 
	 * @return true if the context id is an idle activity.
	 */
	private boolean isIdle(int contextId) {
		return contextId == 0;
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
		if (option == TraceOption.REVEAL_GPU_TRACE) {
			if (isIdle(nextData.cpId)) {	
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
		}
		
		addSample(minIndex, nextData);
		
		int addedLeft  = sampleTimeLine(minLoc, loc, startPixel, midPixel, minIndex, pixelLength, startingTime);
		int addedRight = sampleTimeLine(loc, maxLoc, midPixel, endPixel, minIndex+addedLeft+1, pixelLength, startingTime);
		
		return (addedLeft+addedRight+1);
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
	        boolean debug_search = false;
	        if (debug_search) System.err.printf("findTimeInInterval: %d with index range [%d, %d]\n", time, left_boundary_offset, right_boundary_offset);

		if (left_boundary_offset == right_boundary_offset) return left_boundary_offset;

		long left_index = getRelativeLocation(left_boundary_offset);
		long right_index = getRelativeLocation(right_boundary_offset);
		
		long left_time  = data.getLong(left_boundary_offset);
		long right_time = data.getLong(right_boundary_offset);
		
		if (debug_search) System.err.printf("findTimeInInterval: %d with time range [%d, %d]\n", time, left_time, right_time);

		// apply "Newton's method" to find target time
		while (right_index - left_index > 1) {
			long predicted_index;
			final long time_range = right_time - left_time;
			final double rate = (double)time_range / (right_index - left_index);
			final long mtime  = time_range / 2;
			if (time <= mtime) {
				predicted_index = Math.max((long) ((time - left_time) / rate) + left_index, left_index);
			} else {
				predicted_index = Math.min((right_index - (long) ((right_time - time) / rate)), right_index); 
			}
			
			if (debug_search) System.err.printf("newton predicted index = %d\n", predicted_index);

			// adjust predicted_index so that it differs from the endpoints.
			// without that, the search may fail to converge.
			if (predicted_index <= left_index) 
				predicted_index = left_index + 1;

			if (predicted_index >= right_index)
				predicted_index = right_index - 1;

			if (debug_search) System.err.printf("adjusted predicted index = %d\n", predicted_index);

			long temp = data.getLong(getAbsoluteLocation(predicted_index));

			if (debug_search) System.err.printf("before: indices [%d, %d] time interval [%d, %d] target time = %d rel: [%d, %d] , rate = %f predicted_time = %d\n", left_index, right_index, left_time, right_time, time, temp - left_time, right_time - temp, rate, temp); 
			if (time >= temp) {
				left_index = predicted_index;
				left_time = temp;
			} else {
				right_index = predicted_index;
				right_time = temp;
			}
			if (debug_search) System.err.printf("after: indices [%d, %d] time interval [%d, %d] target time = %d, rate = %f\n", left_index, right_index, left_time, right_time, time, rate);  
		}

		long left_offset = getAbsoluteLocation(left_index);
		return left_offset;

		/*
		long right_offset = getAbsoluteLocation(right_index);

		left_time = data.getLong(left_offset);
		right_time = data.getLong(right_offset);

		// return the closer sample or the maximum sample if the 
		// time is at or beyond the right boundary of the interval
		final boolean is_left_closer = Math.abs(time - left_time) < Math.abs(right_time - time);
		long maxloc = data.getMaxLoc(rank);
		

		if ( is_left_closer ) return left_offset;
		else if (right_offset < maxloc) return right_offset;
		else return maxloc;
		*/
	}
	
	private long getAbsoluteLocation(long relativePosition)
	{
		return data.getMinLoc(rank) + (relativePosition * data.getRecordSize());
	}
	
	private long getRelativeLocation(long absolutePosition)
	{
		return (absolutePosition-data.getMinLoc(rank)) / data.getRecordSize();
	}
	
	
	/**Adds a sample to times and timeLine.*/
	private void addSample( int index, DataRecord datacpid)
	{		
		if (index == listcpid.size())
		{
			this.listcpid.add(datacpid);
		}
		else
		{
			this.listcpid.add(index, datacpid);
		}
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

	/*********************************************************************************************
	 * Removes unnecessary samples:
	 * i.e. if timeLine had three of the same cpid's in a row, the middle one would be superfluous,
	 * as we would know when painting that it should be the same color all the way through.
	 ********************************************************************************************/
	private void postProcess()
	{
		int len = listcpid.size();
		for(int i = 0; i < len-2; i++)
		{
			while(i < len-1 && listcpid.get(i).timestamp==(listcpid.get(i+1).timestamp))
			{
				listcpid.remove(i+1);
				len--;
			}
		}
	}
}
