// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.summary;

import java.util.AbstractMap;
import java.util.TreeMap;

import org.eclipse.swt.graphics.PaletteData;

import edu.rice.cs.hpctraceviewer.data.color.ColorTable;

public class SummaryData 
{
	public PaletteData palette;
	public AbstractMap<Integer, Integer> mapPixelToCount;
	
	public ColorTable colorTable;
	public int totalPixels;
	
	public TreeMap<Integer, Float> cpuBlameMap;
	public TreeMap<Integer, Float> gpuBlameMap;
	
	public float totalCpuBlame;
	public float totalGpuBlame;
	
	
	public SummaryData(
			PaletteData palette,
			AbstractMap<Integer, Integer> mapPixelToCount, 
			ColorTable colorTable, 
			int totalPixels) {
		
		this.palette = palette;
		this.mapPixelToCount = mapPixelToCount;
		this.colorTable = colorTable;
		this.totalPixels = totalPixels;
	}

	public SummaryData(
			PaletteData palette, 
			ColorTable colorTable,
			TreeMap<Integer, Float> cpuBlameMap, 
			float totalCpuBlame,
			TreeMap<Integer, Float> gpuBlameMap, 
			float totalGpuBlame) {
		
		this.palette = palette;
		this.colorTable = colorTable;

		this.cpuBlameMap = cpuBlameMap;
		this.totalCpuBlame = totalCpuBlame;
		
		this.gpuBlameMap = gpuBlameMap;
		this.totalGpuBlame = totalGpuBlame;

		}
}
