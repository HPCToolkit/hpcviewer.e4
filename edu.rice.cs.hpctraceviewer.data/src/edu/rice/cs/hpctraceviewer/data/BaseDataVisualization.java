package edu.rice.cs.hpctraceviewer.data;

import org.eclipse.swt.graphics.Color;

/*******************************************************
 * 
 * Data class to store the information of data to be 
 * 	visualized on a depth view or detail view or any views
 *
 *******************************************************/
public class BaseDataVisualization {
	final public int x_start;
	final public int x_end;
	final public Color color;
	final public int depth;

	public BaseDataVisualization(int x_start, int x_end, int depth, Color color) {
		
		this.x_start = x_start;
		this.x_end = x_end;
		this.color = color;
		this.depth = depth;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		
		return "[" + x_start + ", " + x_end + "] c: " + color +", d: " + depth;
	}
}
