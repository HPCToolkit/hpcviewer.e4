// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.data;

import java.util.ArrayList;
import java.util.List;

public class TimelineDataSet 
{
	public static final TimelineDataSet NULLTimeline = new TimelineDataSet(-1, 0, 0);

	private final List<BaseDataVisualization> list;
	private final int height;
	private final int linenum;
	
	public TimelineDataSet( int linenum, int initSize, int height ) {
	 	
		list = new ArrayList<>(initSize);
	 	this.height = height; 
	 	this.linenum = linenum;
	}
	
	public void add( BaseDataVisualization data ) {
		list.add(data);
	}
	
	public List<BaseDataVisualization> getList() {
		return list;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getLineNumber() {
		return linenum;
	}
	
	public String toString() {
		var buffer = new StringBuilder();
		for(BaseDataVisualization dv: list) {
			buffer.append("(" + dv.x_start + "-" + dv.x_end + ", " + dv.color + ") ");
		}
		return buffer.toString();
	}
}
