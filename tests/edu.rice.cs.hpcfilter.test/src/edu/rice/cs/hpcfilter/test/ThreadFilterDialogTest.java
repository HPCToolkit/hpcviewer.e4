// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcfilter.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.junit.Before;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.StringFilterDataItem;
import edu.rice.cs.hpcfilter.dialog.ThreadFilterDialog;
import edu.rice.cs.hpctest.util.ViewerTestCase;

public class ThreadFilterDialogTest extends ViewerTestCase
{
    private SWTBot bot;

    @Override
    @Before
    public void setUp() {
    	super.setUp();
        bot = new SWTBot();
    }


	@Test
	public void testPopulateDialog() {
		List<FilterDataItem<String>> items = new ArrayList<>();
		Random random = new Random();
		
		for(int i=0; i<20; i++) {
			int rank = random.nextInt(10);
			int thread = random.nextInt(100);
			String label = IdTupleType.LABEL_RANK   + " " + rank + " " +
					   	   IdTupleType.LABEL_THREAD + " " + thread;
			
			FilterDataItem<String> obj = new StringFilterDataItem(label, i<6, i>3);
			items.add(obj);
		}
		
		ThreadFilterDialog dialog = new ThreadFilterDialog(shell, "Select rank/thread to view", items);
		dialog.create();
		
		dialog.getShell().open();

		bot.button("OK").click();
		
		var items2 = dialog.getResult();
		
		assertEquals(items.size(), items2.size());
		
		for(int i=0; i<items.size(); i++) {
			var obj1 = items.get(i);
			var obj2 = items2.get(i);
			
			assertEquals(obj1, obj2);
		}
	}
}
