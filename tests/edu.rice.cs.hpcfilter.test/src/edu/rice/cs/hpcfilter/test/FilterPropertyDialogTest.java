// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcfilter.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.junit.Before;
import org.junit.Test;

import edu.rice.cs.hpcdata.filter.FilterAttribute;
import edu.rice.cs.hpcfilter.cct.FilterPropertyDialog;
import edu.rice.cs.hpcfilter.service.FilterMap;
import edu.rice.cs.hpctest.util.ViewerTestCase;

public class FilterPropertyDialogTest extends ViewerTestCase 
{
    private SWTBot bot;

    @Override
    @Before
    public void setUp() {
    	super.setUp();
        bot = new SWTBot();
    }

	
	@Test
	public void testShowInitDialog() {
		FilterPropertyDialog dialog = new FilterPropertyDialog(shell, null);
		dialog.create();
		
		dialog.getShell().open();

		assertFalse( bot.button("Edit").isEnabled() );
		assertFalse( bot.button("Delete").isEnabled() );
		
		bot.button("Close").click();

		FilterMap map = dialog.getInput();
		
		Iterator<Entry<String, FilterAttribute>> iterator = map.iterator();
		while(iterator.hasNext()) {
			Entry<String, FilterAttribute> entry = iterator.next();
			assertNotNull(entry);
			assertNotNull(entry.getKey());
			assertNotNull(entry.getValue());
		}
	}

}
