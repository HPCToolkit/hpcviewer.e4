// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

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
	IPixelAnalysis EMPTY = new IPixelAnalysis() {
		
		@Override
		public void analysisPixelXY(ImageData detailData, int x, int y, int pixelValue) { /* no-op */ }
		
		@Override
		public void analysisPixelInit(int x) { /* no-op */ }
		
		@Override
		public void analysisPixelFinal(int pixel) { /* no-op */ }
		
		@Override
		public void analysisInit(SpaceTimeDataController dataTraces, ColorTable colorTable) { /* no-op */ }
		
		@Override
		public void analysisFinal(ImageData detailData) { /* no-op */ }
	};
	
	/***
	 * Initialize and prepare the analysis  
	 * 
	 * @param dataTraces {@code SpaceTimeDataController} data of the trace
	 * @param colorTable {@code ColorTable} table of colors
	 * @param ptlService ({@code ProcessTimelineService} a service to get the information of the process timeline
	 */
	void analysisInit(SpaceTimeDataController dataTraces, 
							 ColorTable colorTable);
	
	/***
	 * Initialization phase of the analysis for x-axis pixel
	 * @param x the coordinate of x-axis pixel
	 */
	void analysisPixelInit(int x);
	
	/**
	 * Analyze the pixel
	 * 
	 * @param detailData {@code ImageData} the current image to analyze 
	 * @param x {@code int} x-coordinate of the pixel
	 * @param y {@code int} y-coordinate of the pixel
	 * @param pixelValue {@code int} the value of the pixel in coordinate x and y
	 */
	void analysisPixelXY(ImageData detailData, int x, int y, int pixelValue);
	
	/***
	 * Finalize the current x-axis analysis. 
	 * This function is called after the summary view finalized analyzing y-pixels 
	 * 
	 * @param pixel {@code int} the value of the pixel
	 */
	void analysisPixelFinal(int pixel);
	
	/***
	 * Finalize the analysis
	 * @param detailData
	 */
	void analysisFinal(ImageData detailData);
}
