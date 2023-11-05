package edu.rice.cs.hpctest.trace;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpclocal.LocalDBOpener;
import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctraceviewer.data.Frame;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.ui.base.ISpaceTimeCanvas;
import edu.rice.cs.hpctraceviewer.ui.main.DetailViewPaint;

public class DetailViewPaintTest 
{
	private static final int PIXELS_H = 1000;
	private static final int PIXELS_V = 500;

	private static List<SpaceTimeDataController> listData;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		var experiments = TestDatabase.getExperiments();
		listData = new ArrayList<>();

		for(var exp: experiments) {
			if (exp.getTraceDataVersion() < 0)
				// no trace? skip it
				continue;

			var opener = new LocalDBOpener(exp);
			SpaceTimeDataController stdc = opener.openDBAndCreateSTDC(null);
			assertNotNull(stdc);

			home(stdc, new Frame());

			var attribute = stdc.getTraceDisplayAttribute();
			assertNotNull(attribute);

			attribute.setPixelHorizontal(PIXELS_H);
			attribute.setPixelVertical(PIXELS_V);

			listData.add(stdc);
		}
	}

	private static void home(SpaceTimeDataController stData, Frame frame) {
		frame.begProcess = 0;
		frame.endProcess = stData.getTotalTraceCount();
		
		frame.begTime = 0;
		frame.endTime = stData.getTimeWidth();
		
		stData.getTraceDisplayAttribute().setFrame(frame);
	}

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
			
			data.resetProcessTimeline(numLines);
			
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
			
			gcOrigin.dispose();
			gcMaster.dispose();
			imgOrigin.dispose();
			imgMaster.dispose();
			
		}
	}

}
