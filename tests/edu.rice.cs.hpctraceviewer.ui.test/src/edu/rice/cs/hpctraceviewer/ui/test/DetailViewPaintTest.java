// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.test;

import static org.junit.Assert.*;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

import edu.rice.cs.hpctest.util.BaseTestAllTraceDatabases;
import edu.rice.cs.hpctraceviewer.ui.base.ISpaceTimeCanvas;
import edu.rice.cs.hpctraceviewer.ui.main.DetailViewPaint;

public class DetailViewPaintTest  extends BaseTestAllTraceDatabases
{

	@Test
	public void testStartPainting() {
		Shell shell = new Shell();
		
		for (var data: listData) {
			Image imgMaster = new Image(shell.getDisplay(), PIXELS_H, PIXELS_V);
			Image imgOrigin = new Image(shell.getDisplay(), PIXELS_H, 1);
			
			GC gcMaster = new GC(imgMaster);
			GC gcOrigin = new GC(imgOrigin);
			
			var attribute = data.getTraceDisplayAttribute();
			var numProcs = attribute.getProcessInterval();
			var numLines = Math.min(numProcs, PIXELS_V);
			
			data.resetTracelines(numLines);
			
			ISpaceTimeCanvas stdc = new ISpaceTimeCanvas() {
				
				@Override
				public void setMessage(String message) {
					System.err.println(message);
				}
				
				@Override
				public double getScalePixelsPerTime() {
					return (double)attribute.getPixelHorizontal()/attribute.getTimeInterval();
				}
				
				@Override
				public double getScalePixelsPerRank() {
					return attribute.getScalePixelsPerRank();
				}
			};
			
			DetailViewPaint dvp = new DetailViewPaint(
					shell.getDisplay(), 
					gcMaster, 
					gcOrigin, 
					data, 
					numLines, 
					true, 
					stdc);
			
			assertEquals(numLines, dvp.getNumberOfLines());
			
			var result = dvp.paint(new NullProgressMonitor());
			assertTrue(result);
			
			// no change bounds
			dvp = new DetailViewPaint(
					shell.getDisplay(), 
					gcMaster, 
					gcOrigin, 
					data, 
					numLines, 
					false, 
					stdc);
			
			result = dvp.paint(new NullProgressMonitor());
			assertTrue(result);
			
			gcOrigin.dispose();
			gcMaster.dispose();
			imgOrigin.dispose();
			imgMaster.dispose();
			
		}
	}

}
