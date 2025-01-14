// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctree.test;

import static org.junit.Assert.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.junit.Test;
import org.mockito.Mockito;

import edu.rice.cs.hpctree.internal.ScopeTreeDataProvider;
import edu.rice.cs.hpctree.internal.config.ScopeTreePainter;

public class ScopeTreePainterTest {

	@Test
	public void testGetSelectedSortHeaderCellPainter() {
		var c = ScopeTreePainter.getSelectedSortHeaderCellPainter();
		assertNotNull(c);
	}

	@Test
	public void testGetTreeStructurePainterScopeTreeDataProviderColor() {
		var dataProvider = Mockito.mock(ScopeTreeDataProvider.class);
		
		// test dark color
		var bgColor = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
		
		var painter = ScopeTreePainter.getTreeStructurePainter(dataProvider, bgColor);
		assertNotNull(painter);
		
		// test light color
		bgColor = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
		painter = ScopeTreePainter.getTreeStructurePainter(dataProvider, bgColor);
		assertNotNull(painter);
	}

	@Test
	public void testGetTreeStructurePainterScopeTreeDataProvider() {
		var dataProvider = Mockito.mock(ScopeTreeDataProvider.class);
		var painter = ScopeTreePainter.getTreeStructurePainter(dataProvider);
		assertNotNull(painter);
	}

	@Test
	public void testGetInvTreeStructurePainter() {
		var dataProvider = Mockito.mock(ScopeTreeDataProvider.class);
		var painter = ScopeTreePainter.getInvTreeStructurePainter(dataProvider);
		assertNotNull(painter);
	}

}
