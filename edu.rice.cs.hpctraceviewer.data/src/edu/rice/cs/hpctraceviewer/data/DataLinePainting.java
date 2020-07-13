package edu.rice.cs.hpctraceviewer.data;


import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimeline;

/***
 * 
 * struct class for painting a line
 *
 */
public class DataLinePainting 
{	
	public ProcessTimeline ptl;
	public int depth;
	public int height;
	public double pixelLength;
	public ColorTable colorTable;
	public long begTime;
	public boolean usingMidpoint;
}
