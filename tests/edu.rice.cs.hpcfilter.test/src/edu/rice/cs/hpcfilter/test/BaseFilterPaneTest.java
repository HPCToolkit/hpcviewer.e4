// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcfilter.test;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.swt.layout.FillLayout;
import org.junit.Before;
import org.junit.Test;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcfilter.AbstractFilterPane;
import edu.rice.cs.hpcfilter.BaseFilterPane;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.FilterInputData;
import edu.rice.cs.hpcfilter.StringFilterDataItem;
import edu.rice.cs.hpctest.util.ViewerTestCase;


public class BaseFilterPaneTest extends ViewerTestCase
{
	private Random random = new Random();
	private BaseFilterPane<String> pane;
	private FilterInputData<String> data;
	
	@Before
	@Override
	public void setUp() {
		super.setUp();
		
		List<FilterDataItem<String>> items = new ArrayList<>();
		
		for(int i=0; i<20; i++) {
			int rank = random.nextInt(10);
			int thread = random.nextInt(100);
			String label = IdTupleType.LABEL_RANK   + " " + rank + " " +
					   	   IdTupleType.LABEL_THREAD + " " + thread;
			
			FilterDataItem<String> obj = new StringFilterDataItem(label, i<6, i>3);
			items.add(obj);
		}		
		data = new FilterInputData<>(items);
		pane = new BaseFilterPane<>(shell, AbstractFilterPane.STYLE_INDEPENDENT, data);
		
		shell.setLayout(new FillLayout());
		shell.layout();
		shell.open();
		showWindow();
	}

	
	@Test
	public void testGetEventList() {
		List<FilterDataItem<String>> clist = pane.getEventList(); 
		clist.stream().forEach(item -> {
			assertNotNull(item);
			assertNotNull(item.data);
		});
	}
	
	@Test
	public void testReset() {
		var items = data.getListItems();
		int numItems = items.size();
		
		items.add(new StringFilterDataItem("add Label", false, false));
		data = new FilterInputData<>(items);
		pane.reset(data);
		
		var newList = pane.getFilterList();
		assertTrue(newList.size() == numItems + 1);
	}
}
