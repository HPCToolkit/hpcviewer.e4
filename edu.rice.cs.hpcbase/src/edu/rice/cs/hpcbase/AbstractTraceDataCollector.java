package edu.rice.cs.hpcbase;

import java.util.ArrayList;
import java.util.List;

import edu.rice.cs.hpcdata.db.version4.DataRecord;


/***********************************************************************************
 * 
 * Abstract class to collect traces either locally or remotely.
 * This class provides methods and attributes needed for both local and remote
 * child to handle traces.
 * 
 * @apiNote This class is to handle traces PER LINE, not the whole traces.
 *
 ***********************************************************************************/
public abstract class AbstractTraceDataCollector implements ITraceDataCollector 
{
	
	public static final float NUM_PIXELS_TOLERATED = 1.0f;

	private final int numPixelH;
	
	private List<DataRecord> listcpid;

	private final TraceOption option;
	
	
	/****
	 * Create a trace data collector with revealing GPU traces as the default.
	 * 
	 * @param numPixelH
	 * 			Number of pixels or samples horizontally
	 */
	protected AbstractTraceDataCollector(int numPixelH) {
		this(TraceOption.ORIGINAL_TRACE, numPixelH);
	}
	
	
	/*****
	 * Create a trace data collector with a specific option and number of horizontal pixels
	 * for this particular line.
	 * 
	 * @param option
	 * 			A trace option
	 * @param numPixelH
	 * 			Number of pixels or samples horizontally
	 */
	protected AbstractTraceDataCollector(TraceOption option, int numPixelH) {
		this.option = option;
		this.numPixelH = numPixelH;		
		
		listcpid = new ArrayList<>(numPixelH);
	}
	
	
	protected TraceOption getTraceOption() {
		return option;
	}
	

	@Override
	public boolean isEmpty() {
		return listcpid == null || listcpid.isEmpty();
	}

	@Override
	public int findClosestSample(long time, boolean usingMidpoint) throws Exception {
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
			final long time_current = usingMidpoint ? getTimeMidPoint(mid,mid+1) : listcpid.get(mid).timestamp;
			
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
			var dtime = r.timestamp - l.timestamp;
			float dFraction = (float) dtime/timePerPixel;
			
			if (dFraction < NUM_PIXELS_TOLERATED && !isIdle(r.cpId))
				return low+1;
		}
		// without using midpoint, we adopt the leftmost sample approach.
		// this means whoever on the left side, it will be the painted
		return low;
	}


	@Override
	public long getTime(int sample) {
		if(sample<0 || listcpid == null || listcpid.isEmpty())
			return 0;

		final int last_index = listcpid.size() - 1;
		if(sample>last_index) {
			throw new IllegalArgumentException(sample + ": invalid sample number");
		}
		return listcpid.get(sample).timestamp;
	}

	@Override
	public int getCpid(int sample) {
		if (sample < listcpid.size())
			return listcpid.get(sample).cpId;
		return -1;
	}

	@Override
	public int size() {
		return listcpid.size();
	}

	@Override
	public void shiftTimeBy(long lowestStartingTime) {
		for(int i = 0; i<listcpid.size(); i++)
		{
			DataRecord timecpid = listcpid.get(i);
			if (timecpid.timestamp < lowestStartingTime) {
				/*
				LoggerFactory.getLogger(getClass()).error(rank + ". Trace time " 
															   + timecpid.timestamp 
															   + " is smaller than the begin time" 
															   + lowestStartingTime);
				*/
				continue;
			}
			timecpid.timestamp = timecpid.timestamp - lowestStartingTime;
			
			listcpid.set(i,timecpid);
		}
	}
	

	/***
	 * duplicate data from other object
	 * 
	 * @param traceData: another object to be copied
	 */
	@Override
	public void duplicate(ITraceDataCollector traceData)
	{
		this.listcpid = ((AbstractTraceDataCollector)traceData).listcpid;
	}

	
	@Override
	public void dispose() {
		if (listcpid != null)
			listcpid.clear();
		listcpid = null;
	}
	
	protected int getNumPixelHorizontal() {
		return numPixelH;
	}

	protected List<DataRecord> getListOfCallpathId() {
		return listcpid;
	}
	
	protected void setListOfCallpathId(List<DataRecord> listCpid) {
		this.listcpid = listCpid;
	}
	
	
	/**Adds a sample to times and timeLine.*/
	protected void addSample( int index, DataRecord datacpid)
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
	
	
	protected void addSampleToLastIndex(DataRecord datacpid) {
		addSample(listcpid.size(), datacpid);
	}


	/*********************************************************************************************
	 * Removes unnecessary samples:
	 * i.e. if timeLine had three of the same cpid's in a row, the middle one would be superfluous,
	 * as we would know when painting that it should be the same color all the way through.
	 ********************************************************************************************/
	protected void postProcess()
	{
		for(int i = 0; i < listcpid.size()-2; i++)
		{
			while(i < listcpid.size()-1 && listcpid.get(i).timestamp==(listcpid.get(i+1).timestamp))
			{
				listcpid.remove(i+1);
			}
		}
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
	protected boolean isIdle(int contextId) {
		return contextId == 0;
	}

}
