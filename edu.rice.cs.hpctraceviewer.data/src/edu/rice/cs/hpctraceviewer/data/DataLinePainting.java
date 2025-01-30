// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.data;


import edu.rice.cs.hpcbase.IProcessTimeline;
import edu.rice.cs.hpctraceviewer.data.color.ColorTable;


/***
 * 
 * struct class for painting a line
 *
 */
public class DataLinePainting 
{	
	public IProcessTimeline ptl;
	public int depth;
	public int height;
	public double pixelLength;
	public ColorTable colorTable;
	public long begTime;
	public boolean usingMidpoint;
}
