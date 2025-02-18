// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.rice.cs.hpctest.util.ViewerTestCase;
import edu.rice.cs.hpctraceviewer.data.util.ProcedureClassMap;
import edu.rice.cs.hpctraceviewer.ui.dialog.ProcedureClassDialog;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ProcedureClassDialogTest extends ViewerTestCase
{
    private SWTBot bot;

    @Override
    @Before
    public void setUp() {
    	super.setUp();
        bot = new SWTBot();
    }


	@Test
	public void initTest() {
		ProcedureClassMap pcMap = new ProcedureClassMap(shell.getDisplay());
		ProcedureClassDialog dlg = new ProcedureClassDialog(shell, pcMap);
		
		dlg.create();
		dlg.getShell().open();
		SWTBotShell shell1 = bot.shell("Color mapping");
		shell1.activate();

		var numColumns = bot.table().columnCount();
		assertEquals(3, numColumns);
		
		// test clicking a row column which enables buttons
		var btnDelete = bot.button("Delete");
		assertFalse(btnDelete.isEnabled());
		
		var btnEdit = bot.button("Edit");
		assertFalse(btnEdit.isEnabled());
		
		bot.table().click(1, 0);
		
		assertTrue(btnDelete.isEnabled());
		assertTrue(btnEdit.isEnabled());
		
		assertFalse(dlg.isModified());
	}
}
