// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcfilter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.junit.Before;
import org.junit.Test;

import edu.rice.cs.hpcfilter.cct.FilterInputDialog;
import edu.rice.cs.hpctest.util.ViewerTestCase;

public class FilterInputDialogTest  extends ViewerTestCase
{
    private SWTBot bot;

    @Override
    @Before
    public void setUp() {
    	super.setUp();
        bot = new SWTBot();
    }

    
	@Test
	public void testInitDialog() 
	{
		final String pattern = "initial";
		
		FilterInputDialog dialog = new FilterInputDialog(shell, "Toto", pattern, null);
		dialog.create();
		
		dialog.getShell().open();

		bot.button("OK").click();

		assertEquals(pattern, dialog.getValue());
		assertNotNull(dialog.getAttribute());
	}
}