// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.test;

import static org.junit.Assert.*;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.junit.Before;
import org.junit.Test;

import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctraceviewer.ui.context.BaseTraceContext;
import edu.rice.cs.hpctraceviewer.ui.minimap.SpaceTimeMiniCanvas;
import edu.rice.cs.hpctraceviewer.ui.operation.AbstractTraceOperation;
import edu.rice.cs.hpctraceviewer.ui.operation.ZoomOperation;
import edu.rice.cs.hpctraceviewer.ui.test.internal.TraceTestCase;

public class SpaceTimeMiniCanvasTest extends TraceTestCase {

	@Before
	public void setUp() {
		super.setUp();
		
		final var title = "test-minimap";
		shell.setText(title);
		shell.setMinimumSize(500, 500);
		
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
	}

	@Test
	public void testUpdateViewSpaceTimeDataController() throws Exception {

		var miniMapCanvas = new SpaceTimeMiniCanvas(getTracePart(), shell);
		miniMapCanvas.setLayout(new FillLayout(SWT.HORIZONTAL));
		miniMapCanvas.setSize(400, 400);
		
		shell.layout();
		shell.open();
		
		var experiments = TestDatabase.getExperiments();
		var data = createSTDC(experiments.get(0), 500, 500);
		
		var attributes = data.getTraceDisplayAttribute();
		var frame = attributes.getFrame();
		var numTraces = data.getNumTracelines();
		frame.begProcess = 1;
		frame.endProcess = numTraces-1;
		
		miniMapCanvas.updateView(data);
		
		var pixels = miniMapCanvas.getScalePixelsPerRank();
		assertTrue(pixels > 0);
		
		pixels = miniMapCanvas.getScalePixelsPerTime();
		assertTrue(pixels > 0);
		
		var tracePart = getTracePart();
		IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_BUFFER);
		AbstractTraceOperation operation = new ZoomOperation(data, "zoom", null, context);
		operation.addContext(context);

		// dummy operation
		var event = new OperationHistoryEvent(0, getOperationHistory(), operation);
		miniMapCanvas.historyNotification(event);
		
		// resize operation
		frame = data.getTraceDisplayAttribute().getFrame();
		frame.begProcess++;
		frame.endProcess--;
		data.getTraceDisplayAttribute().setFrame(frame);
		miniMapCanvas.updateView();
		
		showWindow();
	}

}
