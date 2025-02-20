// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.Before;
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
	
	@Before
	@Override
	public void setUp() {
		super.setUp();
		
		shell.setLayout(new FillLayout());

		final var title = "test-canvas";
		shell.setText(title);
	}

	@Test
	public void testSetDataObject() throws Exception {

		var tracePart = getTracePart();
		CanvasAxisY canvas = new CanvasAxisY(tracePart, shell);
		canvas.setLayout(new FillLayout());
		canvas.layout();

		shell.layout();
		shell.open();
		
		var bounds = shell.getBounds();
		canvas.setBounds(bounds);


		var rec = canvas.getBounds();
		assertNotNull(rec);
		
		var experiments = TestDatabase.getExperiments();
		
		// test the y-axis canvas for all databases in the test directory
		for (var exp: experiments) {
			var stdc = createSTDC(exp, rec.height, rec.width);
			
			canvas.setData(stdc);
			canvas.setFocus();
			canvas.redraw();
			
			// simulate notifying canvas axis y to refresh
			BufferRefreshOperation op = new BufferRefreshOperation(stdc, null, null);
			
			var history = getOperationHistory();
			OperationHistoryEvent event = new OperationHistoryEvent(OperationHistoryEvent.DONE, history, op);
			
			canvas.historyNotification(event);

			// show the window for debugging purpose and human test
			// we don't need this for CI
			showWindow(1000);
			
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
				int pixel = imgData.getPixel(i, imgData.y + i);
				var color = imgData.palette.getRGB(pixel);
				
				// make sure we start with the first color
				if (oldColor == null) {
					oldColor = color;
				} else if (!oldColor.equals(color)) {
					numChanges++;
					oldColor = color;
				}
			}
			
			// random mouse hover to trigger a tooltip
			var mouse = new Event();
			mouse.x = imgData.x + imgData.width  / 4;
			mouse.y = imgData.y + imgData.height / 4;

			canvas.notifyListeners(SWT.MouseHover, mouse);

			// make sure we free resources even for the test
			// should make this automatically done
			img.dispose();
			gc.dispose();
			
			// In theory the number of alternate colors can't be bigger than number of id tuples
			// However this is not the case for HD display like retina screen.
			// So the safest way is to assume the number of changes is positive
			if (numTraces > 0) {
				assertTrue(numChanges > 0);
				
				// for retina display, one software pixel = 2 hardware pixels,
				// for ultra hd perhaps one software pixel = 4 hardware pixels (idk, need to check)
				// The number of changes can't be bigger than quarter of the traces even for normal display
				//
				// In the future we have to certain the pixel density without using SWT internal API
				// At the moment it's okay with the constant number 4 which will fail when one software pixel
				//   equals six or eigth hardware pixels on some displays in the future
				assertFalse("changes: " + numChanges + ", traces: " + numTraces, numChanges < numTraces/4);
			}
		}
		canvas.dispose();
	}
}
