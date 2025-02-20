// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.test;

import static org.junit.Assert.assertTrue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.junit.Before;
import org.junit.Test;

import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctraceviewer.ui.minimap.MiniMap;
import edu.rice.cs.hpctraceviewer.ui.test.internal.TraceTestCase;

public class MiniMapTest extends TraceTestCase 
{

	@Before
	public void setUp() {
		super.setUp();
		
		final var title = "test-minimap";
		shell.setText(title);
		shell.setMinimumSize(500, 500);
		
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
	}

	@Test
	public void testSetInput() throws Exception {
		CTabFolder tabFolder = new CTabFolder(shell, SWT.NONE);
		tabFolder.setLayout(new FillLayout(SWT.VERTICAL));
		
		Composite area = new Composite(shell, SWT.NONE);
		area.setLayout(new FillLayout(SWT.VERTICAL));
		
		MiniMap miniMap = new MiniMap(tabFolder, SWT.NONE);
		miniMap.createContent(getTracePart(), null, null, area);
		
		shell.layout();
		shell.open();
		
		var experiments = TestDatabase.getExperiments();
		var data = createSTDC(experiments.get(0), 500, 500);
		miniMap.setInput(data);
		
		var bounds = miniMap.getBounds();
		assertTrue(bounds.height > 0  &&  bounds.width > 0);
	}
}
