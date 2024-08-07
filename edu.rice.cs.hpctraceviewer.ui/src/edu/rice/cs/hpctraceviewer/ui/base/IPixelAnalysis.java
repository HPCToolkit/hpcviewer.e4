// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.base;

import org.eclipse.swt.graphics.ImageData;

import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.color.ColorTable;


/********************************************************
 * 
 * Interface to analyze pixels displayed in the trace view
 *
 ********************************************************/
public interface IPixelAnalysis 
{
	/****
	 * Default implementation of {@code IPixelAnalysis}
	 */
	public static IPixelAnalysis EMPTY = new IPixelAnalysis() {
		
		@Override
		public void analysisPixelXY(ImageData detailData, int x, int y, int pixelValue) {}
		
		@Override
		public void analysisPixelInit(int x) {}
		
		@Override
		public void analysisPixelFinal(int pixel) {}
		
		@Override
		public void analysisInit(SpaceTimeDataController dataTraces, ColorTable colorTable) {}
		
		@Override
		public void analysisFinal(ImageData detailData) {}
	};
	
	/***
	 * Initialize and prepare the analysis  
	 * 
	 * @param dataTraces {@code SpaceTimeDataController} data of the trace
	 * @param colorTable {@code ColorTable} table of colors
	 * @param ptlService ({@code ProcessTimelineService} a service to get the information of the process timeline
	 */
	public void analysisInit(SpaceTimeDataController dataTraces, 
							 ColorTable colorTable);
	
	/***
	 * Initialization phase of the analysis for x-axis pixel
	 * @param x the coordinate of x-axis pixel
	 */
	public void analysisPixelInit(int x);
	
	/**
	 * Analyze the pixel
	 * 
	 * @param detailData {@code ImageData} the current image to analyze 
	 * @param x {@code int} x-coordinate of the pixel
	 * @param y {@code int} y-coordinate of the pixel
	 * @param pixelValue {@code int} the value of the pixel in coordinate x and y
	 */
	public void analysisPixelXY(ImageData detailData, int x, int y, int pixelValue);
	
	/***
	 * Finalize the current x-axis analysis. 
	 * This function is called after the summary view finalized analyzing y-pixels 
	 * 
	 * @param pixel {@code int} the value of the pixel
	 */
	public void analysisPixelFinal(int pixel);
	
	/***
	 * Finalize the analysis
	 * @param detailData
	 */
	public void analysisFinal(ImageData detailData);
}
