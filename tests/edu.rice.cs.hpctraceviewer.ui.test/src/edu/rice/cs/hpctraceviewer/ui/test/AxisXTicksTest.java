// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.junit.Before;
import org.junit.Test;

import edu.rice.cs.hpctest.util.ViewerTestCase;
import edu.rice.cs.hpctraceviewer.data.TraceDisplayAttribute;
import edu.rice.cs.hpctraceviewer.ui.main.AxisXTicks;


public class AxisXTicksTest extends ViewerTestCase 
{
	
	@Before
	@Override
	public void setUp() {
		super.setUp();
		
		shell.setLayout(new FillLayout());

		final var title = "test-axis-x-ticks";
		shell.setText(title);
	}

	
	@Test 
	public void testLabelFormat() {
		new AxisXTicks(0, TimeUnit.NANOSECONDS);
		var format = AxisXTicks.getLabelFormat();
		assertNotNull(format);
		
		var str = format.format(1.1);
		assertTrue(str.length() == 3);
	}
	

	@Test
	public void testComputeTicks() {
		var area = shell.getClientArea();
		
		Canvas canvas = new Canvas(shell, SWT.NONE);
		canvas.setLayout(new FillLayout());
		canvas.layout();
		
		shell.layout();
		shell.open();
		
		int []viewWidths = {100, 200, 415, 1024, 2048};
		long []timeBegin = {0,             100 * 100,    200 * 100,      100 * 10000,  200 * 1000};
		long []timeEnd   = {100 * 1000,   1000 * 1000, 10 * 1000 * 1000, 100 * 1000 * 1000};
		
		TraceDisplayAttribute attribute = new TraceDisplayAttribute();
		var fg = shell.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		var bg = shell.getDisplay().getSystemColor(SWT.COLOR_WHITE);
		
		for (int width: viewWidths) {
			
			Image img = new Image(shell.getDisplay(), width, area.height);
			GC    gc = new GC(img);

			AxisXTicks axisTicks = new AxisXTicks(width, TimeUnit.NANOSECONDS);
			
			attribute.setPixelHorizontal(width);
			
			for (long t1: timeBegin) {
			
				for (long t2: timeEnd) {
					attribute.setTime(t1, t2);
					
					var info = axisTicks.computeTicks(attribute, str -> {return gc.stringExtent(str).x;});
					
					assertNotNull(info);
					assertNotNull(info.dataTimeUnit());
					assertNotNull(info.displayTimeUnit());
					
					assertNotNull(info.tick_labels());
					
					var ticks = info.ticks();
					assertNotNull(ticks);
					assertTrue(ticks.length > 0);
					
					gc.setBackground(bg);
					gc.setForeground(fg);
					
					gc.fillRectangle(0, 0, width, area.height);
					
					var gaps = new HashMap <Integer, Integer>();
					
					int oldTick = Integer.MIN_VALUE;					
					
					for (var tick: ticks) {
						
						// check the tick position
						assertTrue(tick >= 0 && tick > oldTick);
						
						if (oldTick >= 0) {
							// check the gap between ticks in pixels.
							// if the gap is 1 pixel different, it's mostly due to floating-point issue 
							//  or the width is not evenly partitioned.
							// this needs to be addressed in the future. At the moment it's a warning
							int gap = tick - oldTick;
							
							var value = gaps.get(gap);
							if (value != null) {
								value++;
								gaps.replace(gap, value);
							} else {
								gaps.put(gap, 0);
							}
						}
						
						oldTick = tick;
						gc.drawLine(tick, 5, tick, 10);
					}
					var timeUnit = info.displayTimeUnit();
					assertNotNull(timeUnit);
					
					var gapTickLabel = new HashMap<String, Integer>();
					var gapIndexLabel = new HashMap<Integer, Integer>();
					
					int oldX = Integer.MIN_VALUE;
					int oldIndex = Integer.MIN_VALUE;
					double oldTickLabel = Double.NEGATIVE_INFINITY;
					
					var strUnit = attribute.getTimeUnitName(timeUnit);
					
					var format = AxisXTicks.getLabelFormat();
					
					for (var label: info.tick_labels()) {
						var index = label.tickIndex();
						assertTrue(index >= 0 && index < ticks.length);
						assertNotNull(label.tickLabel());
						
						assertTrue( Double.compare(label.tickLabel(), oldTickLabel) > 0);
						
						// check the tick's label: the difference between tick label should be the same
						if (oldTickLabel >= 0) {
							double gap = label.tickLabel() - oldTickLabel;
							
							var strGap = format.format(gap);
							
							var occurences = gapTickLabel.get(strGap);
							if (occurences == null) {
								gapTickLabel.put(strGap, 0);
							} else {
								occurences++;
								gapTickLabel.replace(strGap, occurences);
							}
						}
						// check the index of the label:
						// the gap of the index has to be the same.
						if (oldIndex >= 0) {
							int gap = index - oldIndex;
							var occurences = gapIndexLabel.get(gap);
							
							if (occurences == null) {
								gapIndexLabel.put(gap, 0);
							} else {
								occurences++;
								gapIndexLabel.replace(gap, occurences);
							}
						}
						
						oldTickLabel = label.tickLabel();
						
						int x = ticks[index];
						
						// check the value of x position: it has to be within range, and bigger than previous x
						assertTrue(x >= 0 && x <= width && x > oldX);
						gc.drawLine(x, 5, x, 15);
						
						var str = AxisXTicks.getLabelFormat().format(label.tickLabel()) + strUnit;
						
						gc.drawText(str, x - 5, 18);
						
						oldX = x;
					}
					// if there is different gaps between ticks within a pixel, show a warning
					if (gaps.size() > 1) {
						System.out.printf("- width: %d, time: [%10d, %10d] num uneven gaps: %d / %d : ", width, t1, t2, gaps.size(), ticks.length);
						gaps.forEach((k, v) -> System.out.print(" " + k));
						System.out.println();
					}
					// if the difference is more than a pixel, or more than 2 different gaps, fail
					assertTrue(gaps.size() <= 2);
					
					if (gapTickLabel.size() > 1) {
						System.out.printf("- width: %d, time: [%10d, %10d] num uneven labels: %d / %d : ", width, t1, t2, gapTickLabel.size(), info.tick_labels().size());
						gapTickLabel.forEach((k, v) -> System.out.print(" " + k));
						System.out.println();
					}
					// we don't tolerate different "distance" between ticks' label
					assertTrue(gapTickLabel.size() <= 1);
					
					// paint the axis to check visually
					PaintListener pl = e -> {
						e.gc.drawImage(img, 0, 0);
					};
					canvas.addPaintListener( pl );					
					canvas.redraw();
					
					showWindow();
					
					canvas.removePaintListener(pl);
				}
			}
			gc.dispose();
			img.dispose();
		}
	}
}
