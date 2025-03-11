// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.data;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


/***********
 * Struct class to store attributes of a trace view like:
 * <ul>
 * <li> {@link Frame} : ROI and current position
 * <li> Number of horizontal pixels
 * <li> Number of vertical pixels for the main view
 * <li> Number of vertical pixels for the depth view
 * </ul>
 * <br/>
 * It contains methods to check the bounds and to covert from pixel to position 
 */
public class TraceDisplayAttribute 
{
	public static final String DISPLAY_TIME_UNIT = "prop.disp.tu";
	
	private final Map<Integer, TimeUnit> mapIntegerToUnit;
	private final EnumMap<TimeUnit, String>  mapUnitToString;
	
	private int numPixelsH, numPixelsV;
	private int numPixelsDepthV;
	
	// multiplier for unit time to be displayed 
	private float multiplier;

	private Frame frame;

	// sometimes the time unit is not the same as the displayed time unit
	// the reason is that the time unit can be in millisecond, but the displayed one is in second
	// This is due to issue #20 which complicates a lot of stuff. It simplifies users, but painful to code.
	
	private TimeUnit displayTimeUnit;
	private TimeUnit timeUnit;
	private PropertyChangeSupport support;

	public TraceDisplayAttribute()
	{
		frame = new Frame();

		mapIntegerToUnit = new HashMap<>(7);
		
		mapIntegerToUnit.put(0, TimeUnit.NANOSECONDS);
		mapIntegerToUnit.put(1, TimeUnit.MICROSECONDS);
		mapIntegerToUnit.put(2, TimeUnit.MILLISECONDS);
		mapIntegerToUnit.put(3, TimeUnit.SECONDS);
		mapIntegerToUnit.put(4, TimeUnit.MINUTES);
		mapIntegerToUnit.put(5, TimeUnit.HOURS);
		mapIntegerToUnit.put(6, TimeUnit.DAYS);
		
		mapUnitToString = new EnumMap<>(TimeUnit.class);
		
		mapUnitToString.put(TimeUnit.NANOSECONDS, "ns");
		mapUnitToString.put(TimeUnit.MICROSECONDS, "us");
		mapUnitToString.put(TimeUnit.MILLISECONDS, "ms");
		mapUnitToString.put(TimeUnit.SECONDS, "s");
		mapUnitToString.put(TimeUnit.MINUTES, "min");
		mapUnitToString.put(TimeUnit.HOURS, "hr");
		mapUnitToString.put(TimeUnit.DAYS, "day");
		
		displayTimeUnit = TimeUnit.SECONDS;
		timeUnit = TimeUnit.SECONDS;
		multiplier = 1.0f;
		
		support = new PropertyChangeSupport(displayTimeUnit);
	}

	/****
	 * return the position in the list of a given time unit.
	 * This is an inverse version of {@code getTimeUnit}
	 * @param timeUnit
	 * @return
	 */
	public int getTimeUnitOrdinal(TimeUnit timeUnit) {

		for (Map.Entry<Integer, TimeUnit> entry : mapIntegerToUnit.entrySet()) {
			if (entry.getValue() == timeUnit) {
				return entry.getKey();
			}
		}
		return -1;
	}

	/****
	 * return the time unit for a given ordinal. 
	 * This is an inverse version of {@code getTimeUnitOrdinal}
	 * @param ordinal
	 * @return
	 */
	public TimeUnit getTimeUnit(int ordinal) {
		return mapIntegerToUnit.get(ordinal);
	}
	
	
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}
	
	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}
	
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }
    
    public void setDisplayTimeUnit(TimeUnit displayTimeUnit) {
        this.displayTimeUnit = displayTimeUnit;
        support.firePropertyChange(DISPLAY_TIME_UNIT, this.displayTimeUnit, displayTimeUnit);
    }
    
    public TimeUnit getDisplayTimeUnit() {
    	return displayTimeUnit;
    }
	
    
    public void setTimeUnitMultiplier(float multiplier) {
    	this.multiplier = multiplier;
    }
    
    public float getTimeUnitMultiplier() {
    	return multiplier;
    }
    
	/***
	 * Returns the name of the give time unit
	 * 
	 * @param unit
	 * @return
	 */
	public String getTimeUnitName(TimeUnit unit) {
		return mapUnitToString.get(unit);
	}
	
	
	/****
	 * Compute the suggested time unit for a give space time data
	 * 
	 * @param data
	 * @return
	 */
	public TimeUnit computeDisplayTimeUnit(SpaceTimeDataController data) {
				
		TimeUnit unitInDatabase = data.getTimeUnit();
		
		int unit = 0;
		
		// --------------------------------------------------------------------------
		// find the right unit time (s, ms, us, ns) 
		// To pick the time unit, the time interval should be comfortably within the 
		//  range of the unit. 
		// If the database time unit is ns, and the interval is:
		//   10,000,000,000 --> 10 sec
		//      100,000,000 --> 100ms
		//           20,000 --> 20 us
		//              100 --> 100ns
		// --------------------------------------------------------------------------
		
		do {
			TimeUnit currentUnit = mapIntegerToUnit.get(unit);
			
			long t1 = getTimeBegin();
			long t2 = getTimeEnd();
			long dt = currentUnit.convert(t2 - t1, unitInDatabase);

			if (dt < 5000) {
				// distance between ticks is at least 2 if possible
				// if it's 1 or 0.8, then we should degrade it to higher precision 
				// (higher unit time)
				if (dt <= 2 && unit > 0)
					unit--;
				
				break;
			}
			unit++;

		} while(unit < mapIntegerToUnit.size());
		
		if (unit >= mapIntegerToUnit.size())
			unit = mapIntegerToUnit.size() - 1;
		
		return mapIntegerToUnit.get(unit);
	}
	
	/*****
	 * Retrieve the next higher resolution of the specific time unit
	 * For instance, if the input is Millisecond, it returns Microsecond.
	 * 
	 * @param unit the current unit time (unmodified) 
	 * @return The next higher resolution of the time unit if succeeds, no change otherwise
	 */
	public TimeUnit decrement(TimeUnit unit) {
		Optional<Integer> key = getOrdinal(unit).findFirst();
		if (key.isEmpty())
			throw new IndexOutOfBoundsException("Incorrect time unit: " + unit);
		
		Integer ordinal = key.get();
		if (ordinal == 0)
			// already at the lowest time unit. Can't go further
			return unit;
		
		ordinal--;
		return mapIntegerToUnit.get(ordinal);
	}
	
	
	/*****
	 * Check if we can increase the granularity of time unit.
	 * 
	 * @param unit the current unit time (unmodified) 
	 * @return {@code boolean} true if the current time unit can be decremented to 
	 *         the next lower level unit, false otherwise.
	 */
	public boolean canDecrement(TimeUnit unit) {
		Optional<Integer> key = getOrdinal(unit).findFirst();
		if (key.isEmpty())
			return false;
		
		return key.get()>0;
	}

	
	/****
	 * Check if we can increment the level of time unit.
	 * For instance, the next level of Millisecond is second.
	 * 
	 * If the current time unit is already the highest one, then we can't change 
	 * the granularity.
	 * 
	 * @param unit
	 * @return {@code boolean} true if we can change the granularity, false otherwise
	 */
	public boolean canIncrement(TimeUnit unit) {
		var currentOrd = getOrdinal(unit).findFirst();
		if (currentOrd.isEmpty())
			return false;
		
		return currentOrd.get() < mapIntegerToUnit.size()-1;
	}
	
	
	/****
	 * Get the lower resolution of the given time unit.
	 * If the input is Millisecond, it returns Second.
	 * 
	 * @param unit
	 * @return The next higher level of time unit if success, 
	 *         the unit itself if it reaches the maximum granularity
	 */
	public TimeUnit increment(TimeUnit unit) {
		Optional<Integer> key = getOrdinal(unit).findFirst();
		if (key.isEmpty())
			throw new IndexOutOfBoundsException("Incorrect time unit: " + unit);
		
		Integer ordinal = key.get();
		if (ordinal == mapIntegerToUnit.size()-1)
			// If we have the highest time unit, either throw an exception or return the maximum unit
			// At the moment returning itself is better for the caller to avoid too much try-catch
			return unit;			

		ordinal++;
		return mapIntegerToUnit.get(ordinal);
	}
	
	private Stream<Integer> getOrdinal(TimeUnit unit) {
		return mapIntegerToUnit.entrySet()
				.stream()
				.filter(entry -> unit.equals(entry.getValue()))
				.map(Map.Entry::getKey);
	}
	
	/*************************************************************************
	 * Asserts the process bounds to make sure they're within the actual
	 * bounds of the database, are integers, and adjusts the process zoom 
	 * button accordingly.
	 *************************************************************************/
	public void assertProcessBounds(int maxProcesses)
	{
		if (frame.begProcess < 0)
			frame.begProcess = 0;
		if (frame.endProcess > maxProcesses)
			frame.endProcess = maxProcesses;
	}
	
	/**************************************************************************
	 * Asserts the time bounds to make sure they're within the actual
	 * bounds of the database and adjusts the time zoom button accordingly.
	 *************************************************************************/
	public void assertTimeBounds(long maxTime)
	{
		if (frame.begTime < 0)
			frame.begTime = 0;
		if (frame.endTime > maxTime)
			frame.endTime = maxTime;
	}
	
	
	public void setFrame(Frame frame)
	{
		this.frame = frame;
	}
	
	public Frame getFrame()
	{
		return frame;
	}
	
	public void setProcess(int p1, int p2)
	{
		frame.begProcess = p1;
		frame.endProcess = p2;
		
		frame.fixPosition();
	}
	
	public int getProcessBegin()
	{
		return frame.begProcess;
	}
	
	public int getProcessEnd()
	{
		return frame.endProcess;
	}

	public int getProcessInterval()
	{
		return frame.endProcess - frame.begProcess;
	}
	
	public void setTime(long t1, long t2)
	{
		frame.begTime = t1;
		frame.endTime = t2;
	}
	
	public long getTimeBegin()
	{
		return frame.begTime;
	}
	
	public long getTimeEnd()
	{
		return frame.endTime;
	}
	
	public long getTimeInterval()
	{
		// make sure we have positive time interval, even if users selects 0 time
		return Math.max(1, frame.endTime - frame.begTime);
	}
	
	public boolean sameTrace(TraceDisplayAttribute other)
	{
		return frame.begTime==other.frame.begTime && frame.endTime==other.frame.endTime &&
			   frame.begProcess==other.frame.begProcess && frame.endProcess==other.frame.endProcess &&
			   numPixelsH==other.numPixelsH && numPixelsV==other.numPixelsV;
	}
	
	public void setDepth(int depth)
	{
		frame.depth = depth;
	}
	
	public int getDepth()
	{
		return frame.depth;
	}
	
	public void setPosition(Position p)
	{
		frame.position = p;
	}
	
	public Position getPosition()
	{
		return frame.position;
	}
	
	public int getDepthPixelVertical() {
		return numPixelsDepthV;
	}
	
	public void setDepthPixelVertical(int pixels) {
		numPixelsDepthV = pixels;
	}
	
	public int getPixelHorizontal() {
		return numPixelsH;
	}
	
	public void setPixelHorizontal(int numPixelsH) {
		this.numPixelsH = numPixelsH;
	}
	
	public int getPixelVertical() {
		return numPixelsV;
	}
	
	public void setPixelVertical(int numPixelsV) {
		this.numPixelsV = numPixelsV;
	}
	
	public double getScalePixelsPerRank()
	{
		return (double)numPixelsV / getProcessInterval();
	}

		
	public int convertPixelToRank(int pixelY) 
	{
		int process = 0;
		
    	//need to do different things if there are more traces to paint than pixels
    	if(numPixelsV > getProcessInterval())
    	{
    		process = (int)(getProcessBegin()+pixelY/getScalePixelsPerRank());
    	}
    	else
    	{
    		process = (getProcessBegin()+(pixelY*(getProcessInterval()))/numPixelsV);
    	}
    	return process;
	}
	
	public int convertRankToPixel(int process)
	{
		int pixel = 0;
		if (numPixelsV > getProcessInterval()) 
		{
			pixel = (int) ((process - getProcessBegin()) * getScalePixelsPerRank());
		}
		else {
			pixel = (process - getProcessBegin()) * numPixelsV / getProcessInterval();
		}
		return pixel;
	}
	
	public long convertPixelToTime(int pixelX)
	{
		double pixelsPerTime = numPixelsH / (double) getTimeInterval();
		return (long) (getTimeBegin() + (long) pixelX / pixelsPerTime);
	}
	
	/***
	 * Check if two attribute instances have the same depth attribute
	 * 
	 * @param other
	 * @return
	 */
	public boolean sameDepth(TraceDisplayAttribute other)
	{
		return frame.begTime==other.frame.begTime && frame.endTime==other.frame.endTime &&
			   numPixelsH==other.numPixelsH && numPixelsDepthV==other.numPixelsDepthV;
	}
	
	/***
	 * Copy from another attribute
	 * @param other
	 */
	public void copy(TraceDisplayAttribute other)
	{
		frame.begTime = other.frame.begTime;
		frame.endTime = other.frame.endTime;
		frame.begProcess = other.frame.begProcess;
		frame.endProcess = other.frame.endProcess;
		numPixelsH = other.numPixelsH;
		numPixelsV = other.numPixelsV;
		numPixelsDepthV = other.numPixelsDepthV;
	}
	
	/****
	 * return a new duplicate of this attributes
	 * 
	 * @return a new image attribute
	 */
	public TraceDisplayAttribute duplicate() 
	{
		TraceDisplayAttribute att = new TraceDisplayAttribute();
		att.copy(this);
		return att;
	}
	
	public String toString()
	{
		return  "T [ " + frame.begTime + ","  + frame.endTime+ " ]" +
				"P [ " + frame.begProcess + "," + frame.endProcess + " ]" + 
				" PH: " + numPixelsH + " , PV: " + numPixelsV;
	}
}
