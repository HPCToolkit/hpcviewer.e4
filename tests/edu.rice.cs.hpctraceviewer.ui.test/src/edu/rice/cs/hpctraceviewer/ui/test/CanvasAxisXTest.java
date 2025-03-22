// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.test;

import static org.junit.Assert.*;

import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctraceviewer.ui.main.CanvasAxisX;
import edu.rice.cs.hpctraceviewer.ui.operation.BufferRefreshOperation;
import edu.rice.cs.hpctraceviewer.ui.test.internal.TraceTestCase;

@RunWith(SWTBotJunit4ClassRunner.class)
public class CanvasAxisXTest extends TraceTestCase
{
	
	@Before
	@Override
	public void setUp() {
		super.setUp();

		final var title = "test-x-axis";
		shell.setText(title);
		
		shell.setLayout(new FillLayout());
		shell.layout();
		
		shell.open();
	}

	@Test
	public void testSetDataObject() throws Exception {

		var tracePart = getTracePart();
		CanvasAxisX canvas = new CanvasAxisX(tracePart, shell);
		canvas.setLayout(new FillLayout());
		
		var bounds = shell.getClientArea();
		assertTrue(bounds.height > 0 && bounds.width > 0);
		
		canvas.setSize(bounds.width, bounds.height);
		var rec = canvas.getClientArea();
		assertTrue(rec.width > 0 && rec.height > 0);
		
		var experiments = TestDatabase.getExperiments();
		assertNotNull(experiments);
		
		// test the y-axis canvas for all databases in the test directory
		for(var exp: experiments) {
			var stdc = createSTDC(exp, rec.height, rec.width);
			
			System.out.println("Testing " + exp.getDirectory());
			
			canvas.setData(stdc);
			
			showWindow(1000);

			var attributes = stdc.getTraceDisplayAttribute();
			var deltaTime  = attributes.getTimeInterval();
			var timeBegin  = attributes.getTimeBegin();
			var timeChunk  = deltaTime / 10;
			
			for(int i=0; i<8; i++) {
				// simulate notifying canvas axis y to refresh
				BufferRefreshOperation op = new BufferRefreshOperation(stdc, null, null);
				
				var history = getOperationHistory();
				OperationHistoryEvent event = new OperationHistoryEvent(OperationHistoryEvent.DONE, history, op);
				
				// hack to simulate zoom-in
				var timeStart  = timeBegin + timeChunk * i;
				var timeEnd    = timeBegin + deltaTime - (timeChunk * i);
				attributes.setTime(timeStart, timeEnd);
				
				assertEquals(timeStart, attributes.getTimeBegin());
				assertEquals(timeEnd, attributes.getTimeEnd());
				
				canvas.historyNotification(event);
				
				showWindow();
			}
		}
	}
}
