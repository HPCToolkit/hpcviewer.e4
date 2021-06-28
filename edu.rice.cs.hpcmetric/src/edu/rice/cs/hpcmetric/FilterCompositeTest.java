package edu.rice.cs.hpcmetric;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcmetric.AbstractFilterComposite.Data;

public final class FilterCompositeTest {

	public static void main(String[] args) {
		System.out.println("Test start");
		
		final Display display = new Display();
		final Shell   shell   = new Shell(display);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(shell);
		
		List<Data> list = new ArrayList<AbstractFilterComposite.Data>();
		String []labels = new String[] {"check", "one", "two", "three", "four"};
		
		for (int i=0; i<100; i++)  {
			Data data = new Data();
			data.values = new ArrayList<Object>(4);
			
			for (int j=0; j<5; j++) {
				if (j == 0)
					data.values.add(Boolean.valueOf(i%2 == 0));
				else
					data.values.add("data value " + i + "." + j);
			}
			list.add(data);
		}
		AbstractFilterComposite<Data> c = new AbstractFilterComposite<Data>(shell, SWT.NONE, list, labels) {
			
			@Override
			protected void createAdditionalButton(Composite parent) {}
		};
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(shell);
		
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();


		System.out.println("Test end");
	}

}
