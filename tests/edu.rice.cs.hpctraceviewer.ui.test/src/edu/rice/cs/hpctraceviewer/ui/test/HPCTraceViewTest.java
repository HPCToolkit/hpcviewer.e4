// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.test;

import static org.junit.Assert.*;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.junit.Test;
import org.mockito.Mockito;

import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctraceviewer.ui.main.HPCTraceView;
import edu.rice.cs.hpctraceviewer.ui.test.internal.TraceTestCase;

public class HPCTraceViewTest extends TraceTestCase 
{

	@Test
	public void testCreateContent() throws Exception {
		final var title = "test-stdc";
		shell.setText(title);
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		var tracePart = getTracePart();
		var eventBroker = Mockito.mock(IEventBroker.class);
		
		CTabFolder tabFolder = new CTabFolder(shell, SWT.NONE);
		
		var traceView = new HPCTraceView(tabFolder, SWT.NONE);

		Composite tabArea = new Composite(tabFolder, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(tabArea);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tabArea);
		
		shell.open();
		shell.layout();

		traceView.createContent(tracePart, null, eventBroker, tabArea);
		traceView.setControl(tabArea);
		
		var experiments = TestDatabase.getExperiments();
		assertNotNull(experiments);
		assertTrue(experiments.size() > 0);
		
		// onlyo one database at a time
		var exp = experiments.get(0);
		
		var bounds = tabArea.getBounds();
		
		var stdc = createSTDC(exp, bounds.width, bounds.height);
		traceView.setInput(stdc);
		
		var actions = traceView.getActions();
		assertNotNull(actions);
		
		actions.home();

		assertFalse(actions.canGoDown());
		assertFalse(actions.canGoUp());
		assertFalse(actions.canGoLeft());
		assertFalse(actions.canGoRight());
		
		assertFalse(actions.canTimeZoomOut());
		assertFalse(actions.canProcessZoomOut());
		assertTrue(actions.canTimeZoomIn());
		assertTrue(actions.canProcessZoomIn());
	}

}
