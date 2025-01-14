// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.context.BaseTraceContext;
import edu.rice.cs.hpctraceviewer.ui.internal.TraceEventData;
import edu.rice.cs.hpctraceviewer.ui.main.SpaceTimeDetailCanvas;
import edu.rice.cs.hpctraceviewer.ui.test.internal.TraceTestCase;
import edu.rice.cs.hpctraceviewer.ui.util.IConstants;


/****
 * Minimal test to display and do some operations on the main canvas
 */
public class SpaceTimeDetailCanvasTest extends TraceTestCase
{

	@Before
	public void setUp() {
		super.setUp();
		
		final var title = "test-stdc";
		shell.setText(title);
		shell.setMinimumSize(500, 500);
		
		// make sure it's grid layout
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(shell);
		GridDataFactory.fillDefaults().grab(true, true).hint(500, 500).applyTo(shell);
		
		shell.layout();
		shell.open();
	}

	@Test
	/**
	 * Test to make sure we don't crash when handling events
	 * @throws Exception
	 */
	public void testEvents() throws Exception {
		var tracePart = getTracePart();
		var dataRecord = createCanvas(tracePart);
		var canvas = dataRecord.canvas;

		var map = new HashMap<String, Object>(2);
		var eventData = new TraceEventData(dataRecord.data, null, Integer.valueOf(3));
		map.put(IEventBroker.DATA, eventData);
		
		var event = new org.osgi.service.event.Event(IConstants.TOPIC_COLOR_MAPPING, map);		
		canvas.handleEvent(event);

		// change of depth 
		event = new org.osgi.service.event.Event(IConstants.TOPIC_DEPTH_UPDATE, map);
		canvas.handleEvent(event);

		// new filter ranks
		event = new org.osgi.service.event.Event(IConstants.TOPIC_FILTER_RANKS, map);
		canvas.handleEvent(event);
		
		canvas.widgetDisposed(null);
		canvas.dispose();
	}
	
	@Test
	public void testSetDataSpaceTimeDataController() throws Exception {
		var tracePart = getTracePart();
		var dataRecord = createCanvas(tracePart);
		var stdc = dataRecord.canvas;
		
		showWindow();
		
		// initial setup: we cannot zoom out, cannot move up, left, down or right
		assertFalse(stdc.canProcessZoomOut());
		assertFalse(stdc.canTimeZoomOut());
		
		assertTrue(stdc.canProcessZoomIn());
		assertTrue(stdc.canTimeZoomIn());
		
		assertFalse(stdc.canGoDown());
		assertFalse(stdc.canGoLeft());
		assertFalse(stdc.canGoRight());
		assertFalse(stdc.canGoUp());
		
		var opHistory = tracePart.getOperationHistory();
		when(opHistory.execute(any(), any(), any())).then( (arg)-> {
			// mockito bug? On ARM64, this mocked test is still called by MiniMapTest
			if (stdc.isDisposed())
				return Status.CANCEL_STATUS;
			
			IUndoableOperation op = arg.getArgument(0);
			emulateOperation(op, stdc);
			
			// we assume the operation always success (99% true)
			return Status.OK_STATUS;
		});
		
		// zoom-in x-axis: we should be able to move left and right
		stdc.timeZoomIn();
		assertTrue(stdc.canTimeZoomOut());
		assertTrue(stdc.canGoLeft());
		stdc.goLeft();
		assertTrue(stdc.canGoRight());
		stdc.goRight();
		
		// emulate x-axis zoom out
		// to make sure we reach max zoom, we call zoom out twice
		stdc.timeZoomOut();
		stdc.timeZoomOut();
		assertFalse(stdc.canTimeZoomOut());
		assertFalse(stdc.canGoLeft());
		assertFalse(stdc.canGoRight());
		
		// emulate y-axis zoom in
		stdc.processZoomIn();
		assertTrue(stdc.canProcessZoomOut());
		assertTrue(stdc.canGoDown());
		stdc.goDown();
		assertTrue(stdc.canGoUp());
		stdc.goUp();
		
		// emulate y-axis zoom out again
		// to make sure we reach max zoom, we call zoom out twice
		stdc.processZoomOut();
		stdc.processZoomOut();
		assertFalse(stdc.canProcessZoomOut());
		assertFalse(stdc.canGoDown());
		assertFalse(stdc.canGoUp());
		
		// manual zoom in test
		emulateSelectRegion(stdc);
		
		// test to simulate a mouse click
		emulateClickMouse(stdc);
		
		stdc.widgetDisposed(null);
		stdc.dispose();
	}

	
	private TraceCanvasRecord createCanvas(ITracePart tracePart) throws Exception {
		var bounds = shell.getBounds();
		
		Shell myShell = new Shell(shell);
		myShell.setBounds(bounds);
		
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(myShell);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(myShell);
		
		var eventBroker = Mockito.mock(IEventBroker.class);

		var experiments = TestDatabase.getExperiments();
		
		// test only one database. If we test more than 1, trace viewer will fail
		// or we have to instantiate a new ITracePart.
		// It is unfortunate that we designed one trace view to only support one database.
		
		assertFalse (experiments == null || experiments.size() == 0);

		Composite area = new Composite(myShell, SWT.NONE);		
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(area);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(area);
		
		var stdc = new SpaceTimeDetailCanvas(tracePart, eventBroker, area);

		Composite labelArea = new Composite(area, SWT.NONE);
		stdc.setLabels(labelArea);
		
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(stdc);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(stdc);
		
		myShell.open();
		myShell.layout();
		
		var exp = experiments.get(0);
		var data = createSTDC(exp, bounds.width, bounds.height);
		stdc.setData(data);

		return new TraceCanvasRecord(data, stdc);
	}
	
	private void emulateClickMouse(SpaceTimeDetailCanvas stdc) {
		emulateMouseUpAndDown(stdc, 1);
	}

	
	private void emulateSelectRegion(SpaceTimeDetailCanvas stdc) {
		emulateMouseUpAndDown(stdc, 10);
	}
	
	/***
	 * test to simulate to select a region to zoom-in
	 * 
	 * @param stdc
	 */
	private void emulateMouseUpAndDown(SpaceTimeDetailCanvas stdc, int delta) {
		// make sure the selection is within the region 
		//
		var bounds = stdc.getClientArea();
		
		int mid = (bounds.width + bounds.x) >> 1;

		var mouseEvent = new Event();
		mouseEvent.button = 1;
		mouseEvent.widget = stdc;
		mouseEvent.x = bounds.x + mid - delta;
		mouseEvent.y = bounds.y + mid - delta;
		var mouseDownEvent = new MouseEvent(mouseEvent);
		stdc.mouseDown(mouseDownEvent);
		
		mouseEvent.x = bounds.x + mid + delta;
		mouseEvent.y = bounds.y + mid + delta;
		var mouseUpEvent = new MouseEvent(mouseEvent);
		stdc.mouseUp(mouseUpEvent);
	}
	
	
	/***
	 * Emulate Eclipse history operation by executing pre-op, op, and post-op.
	 * 
	 * @param operation
	 * 			Eclipse operation
	 * @param stdc
	 * 			main data for the trace
	 */
	private void emulateOperation(IUndoableOperation operation, SpaceTimeDetailCanvas stdc) {
		// about to execute an asynchronous operation
		OperationHistoryEvent eventAbout = new OperationHistoryEvent(
				OperationHistoryEvent.ABOUT_TO_EXECUTE, 
				getOperationHistory(), 
				operation);
		
		BaseTraceContext context = new BaseTraceContext(BaseTraceContext.CONTEXT_OPERATION_TRACE);
		operation.addContext(context);
		
		stdc.historyNotification(eventAbout);
		
		var progressMonitor = new NullProgressMonitor();
		
		// Useless dummy execution operation, assume it always returns OK
		// .... we should wait until the execution is done, but I don't know how to check
		try {
			var status = operation.execute(progressMonitor, null);
			assertTrue(status == Status.OK_STATUS);

		} catch (ExecutionException e) {
			// shouldn't happen, unless something wrong with the platform
			fail(e.getMessage());
		}
		
		// execution has done
		OperationHistoryEvent eventDone = new OperationHistoryEvent(
				OperationHistoryEvent.DONE, 
				getOperationHistory(), 
				operation);
		
		stdc.historyNotification(eventDone);	
		
		// end of operation
		var label = operation.getLabel();
		assertNotNull(label);
		assertTrue(label.length() > 1);
		
		progressMonitor.setCanceled(true);
	}
	
	
	/****
	 * Record to hold data controller and canvas
	 */
	public record TraceCanvasRecord(SpaceTimeDataController data, SpaceTimeDetailCanvas canvas) {};
}
