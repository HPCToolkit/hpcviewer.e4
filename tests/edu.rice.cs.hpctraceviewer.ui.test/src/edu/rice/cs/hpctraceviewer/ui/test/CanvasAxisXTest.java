// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.test;

import static org.junit.Assert.*;

import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctraceviewer.ui.main.CanvasAxisX;
import edu.rice.cs.hpctraceviewer.ui.operation.BufferRefreshOperation;
import edu.rice.cs.hpctraceviewer.ui.test.internal.TraceTestCase;

@RunWith(SWTBotJunit4ClassRunner.class)
public class CanvasAxisXTest extends TraceTestCase
{

	@Test
	public void testSetDataObject() throws Exception {
		final var title = "test-canvas";
		shell.setText(title);
		shell.open();

		var tracePart = getTracePart();
		CanvasAxisX canvas = new CanvasAxisX(tracePart, shell);
		
		var bounds = shell.getBounds();
		canvas.setBounds(bounds);
		shell.pack();

		var rec = canvas.getBounds();
		assertNotNull(rec);
		
		var experiments = TestDatabase.getExperiments();
		assertNotNull(experiments);
		
		// test the y-axis canvas for all databases in the test directory
		for(var exp: experiments) {
			var stdc = createSTDC(exp, rec.height, rec.width);
			
			canvas.setData(stdc);
			canvas.setFocus();
			canvas.redraw();

			var attributes = stdc.getTraceDisplayAttribute();
			var deltaTime  = stdc.getMaxEndTime() - stdc.getMinBegTime();
			var timeChunk  = deltaTime / 10;
			
			for(int i=1; i<5; i++) {
				// simulate notifying canvas axis y to refresh
				BufferRefreshOperation op = new BufferRefreshOperation(stdc, null, null);
				
				var history = getOperationHistory();
				OperationHistoryEvent event = new OperationHistoryEvent(OperationHistoryEvent.DONE, history, op);
				
				canvas.historyNotification(event);
				
				// hack to simulate zoom-in
				var timeStart  = timeChunk * i;
				var timeEnd    = deltaTime - (timeChunk * i);
				attributes.setTime(timeStart, timeEnd);
			}
		}
	}
}
