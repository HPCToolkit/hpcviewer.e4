// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.data;

import java.io.Serializable;



/***********************************************************************************
 * 
 * Frame class to store ROI (process and time range) and cursor position 
 *  	(time, process and depth)
 *
 ***********************************************************************************/
public class Frame implements Serializable
{
	
	private static final long serialVersionUID = 1L;

	/**The first and last process being viewed now*/
    public long begTime, endTime;
    
    /**The first and last time being viewed now*/
    public int begProcess, endProcess;
	
	public Position position;
	
	/**The depth of the frame saved*/
	public int depth;
	
	public Frame()
	{
		begTime = endTime = 0L;
		begProcess = endProcess = 0;
		position = new Position(0,0);
		depth = 0;
	}
	
	/****
	 * initialize frame with ROI and the cursor position (time, process and depth)
	 * if the position is not within the range, it will automatically adjust it
	 * into the middle of ROI
	 * 
	 * @param timeBeg
	 * @param timeEnd
	 * @param ProcBeg
	 * @param ProcEnd
	 * @param depth
	 * @param time
	 * @param process
	 */
	public Frame(long timeBeg, long timeEnd, int ProcBeg, int ProcEnd,
			int depth, long time, int process)
	{
		position	= new Position(time, process);
		set(timeBeg, timeEnd, ProcBeg, ProcEnd);

		this.depth  = depth;
	}
	
	/****
	 * initialize frame by copying with the specified frame
	 * if the position is not within the range, it will automatically adjust it
	 * into the middle of ROI
	 * 
	 * @param frame
	 */
	public Frame(Frame frame)
	{
		this.begProcess = frame.begProcess;
		this.endProcess = frame.endProcess;
		this.begTime    = frame.begTime;
		this.endTime    = frame.endTime;
		this.depth	 	= frame.depth;
		this.position	= new Position(frame.position.time, frame.position.process);
	}
	
	public Frame(Position position)
	{
		this.position = position;
	}
	
	/*****
	 * set new value of the frame
	 * 
	 * @param begTime : begin time
	 * @param endTime : end time
	 * @param begProcess : begin process
	 * @param endProcess : end process
	 *****/
	public void set(long begTime, long endTime, int begProcess, int endProcess)
	{
		this.begProcess = Math.max(0, begProcess);
		this.endProcess = endProcess;
		this.begTime	= Math.max(0, begTime);
		this.endTime	= endTime;
		
		fixPosition();
	}
	
	public boolean equals(Frame other)
	{
		return (begProcess == other.begProcess
			&& begTime == other.begTime
			&& endProcess == other.endProcess
			&& endTime == other.endTime
			&& depth == other.depth
			&& position.isEqual(other.position) );
	}
	
	public boolean equalDimension(Frame other) {
		return (begProcess == other.begProcess
				&& begTime == other.begTime
				&& endProcess == other.endProcess
				&& endTime == other.endTime
				&& depth == other.depth);
	}
	
	/** Sets the selected process to the middle if it is outside the bounds.*/
	public void fixPosition(){
		if (position.process >= endProcess
				|| position.process < begProcess ) {
			// if the current process is beyond the range, make it in the middle
			position.process = (begProcess + endProcess) >> 1;
		}
		if (position.time <= begTime || position.time >= endTime) {
			// if the current time is beyond the range, make it in the middle
			position.time = (begTime + endTime ) >> 1;
		}
		assert (position.time >= 0 && position.process >= 0 && begTime >= 0 && endTime >= 0);
	}

	@Override
	public String toString() {
		String time = "[ " + (begTime/1000)/1000.0 + "s, " + (endTime/1000)/1000.0+"s ]";
		String proc = " and [ " + begProcess + ", " + endProcess + " ]";
		return time + proc;
	}
}
