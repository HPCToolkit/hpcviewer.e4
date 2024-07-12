// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctest.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcfilter.AbstractFilterPane;
import edu.rice.cs.hpcfilter.BaseFilterPane;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.FilterInputData;
import edu.rice.cs.hpcfilter.StringFilterDataItem;

public class BaseFilterPaneTest {

	public static void main(String[] args) {
		
		final Display display = new Display();
		final Shell   shell   = new Shell(display);
		shell.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		shell.setLayout(new GridLayout());
		
		shell.setText("hello world");
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
		
		FilterInputData<String> data = new FilterInputData<>(items);
		
		BaseFilterPane<String> pane = new BaseFilterPane<>(shell, AbstractFilterPane.STYLE_INDEPENDENT, data) ;
		 
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		List<FilterDataItem<String>> clist = pane.getEventList(); 
		clist.forEach( item -> {
			System.out.println(item.data + ": " + item.isChecked()); 
		});
		 
		display.dispose();


		System.out.println("Test end");
	}

}
