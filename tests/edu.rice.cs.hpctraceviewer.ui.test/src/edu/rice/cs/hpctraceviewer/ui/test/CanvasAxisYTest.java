// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctraceviewer.ui.main.CanvasAxisY;
import edu.rice.cs.hpctraceviewer.ui.operation.BufferRefreshOperation;
import edu.rice.cs.hpctraceviewer.ui.test.internal.TraceTestCase;

@RunWith(SWTBotJunit4ClassRunner.class)
/***
 * Smoke test to make sure it doesn't crash.
 * It doesn't check the validity of the canvas and layout yet.
 */
public class CanvasAxisYTest extends TraceTestCase 
{

	@Test
	public void testSetDataObject() throws Exception {
		final var title = "test-canvas";
		shell.setText(title);
		shell.open();

		var tracePart = getTracePart();
		CanvasAxisY canvas = new CanvasAxisY(tracePart, shell);
		
		var bounds = shell.getBounds();
		canvas.setBounds(bounds);
		shell.pack();

		var rec = canvas.getBounds();
		assertNotNull(rec);
		
		var experiments = TestDatabase.getExperiments();
		
		// test the y-axis canvas for all databases in the test directory
		for(var exp: experiments) {
			var stdc = createSTDC(exp, rec.height, rec.width);
			
			canvas.setData(stdc);
			canvas.setFocus();
			canvas.redraw();
			
			// simulate notifying canvas axis y to refresh
			BufferRefreshOperation op = new BufferRefreshOperation(stdc, null, null);
			
			var history = getOperationHistory();
			OperationHistoryEvent event = new OperationHistoryEvent(OperationHistoryEvent.DONE, history, op);
			
			canvas.historyNotification(event);
			
			// check the drawn pixels
			Image img = new Image(canvas.getDisplay(), canvas.getBounds());
			GC gc = new GC(canvas);
			
			gc.copyArea(img, 0, 0);

			var imgData = img.getImageData();
			RGB oldColor = null;
			int numChanges = 0;
			int numTraces  = stdc.getNumTracelines();
			
			// iterate all y-axis of the canvas to count the number of alternate colors
			// if we have blue, yellow, blue, then the number of changes is 3
			
			for(int i=0; i<imgData.height; i++) {
				int pixel = imgData.getPixel(5, imgData.y + i);
				var color = imgData.palette.getRGB(pixel);
				
				// make sure we start with the first color
				if (oldColor == null) {
					oldColor = color;
				} else if (!oldColor.equals(color)) {
					numChanges++;
					assertFalse(numChanges > numTraces);
					oldColor = color;
					
					var mouse = new Event();
					mouse.x = 6;
					mouse.y = imgData.y + i;
					canvas.notifyListeners(SWT.MouseHover, mouse);
				}
			}
			// make sure we free resources even for the test
			// should make this automatically done
			img.dispose();
			gc.dispose();
			
			// number of alternate colors can't be bigger than number of id tuples
			// this condition should work in all cases
			assertTrue(numChanges > 0 && numChanges <= numTraces);
		}
		canvas.dispose();
	}
}
