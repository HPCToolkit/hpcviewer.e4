package edu.rice.cs.hpctest.filter.cct;

import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcdata.filter.FilterAttribute;
import edu.rice.cs.hpcfilter.cct.FilterPropertyDialog;
import edu.rice.cs.hpcfilter.service.FilterMap;

public class FilterPropertyDialogTest {
	

	
	////////////////////////////////////////////////////////////////////////////////////////////////
	// 
	// unit test
	//
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	static public void main(String []argv) {
		
		final Shell shell = new Shell();
		
		FilterPropertyDialog dlg = new FilterPropertyDialog(shell, null);
		dlg.open();
		FilterMap map = dlg.getInput();
		
		Iterator<Entry<String, FilterAttribute>> iterator = map.iterator();
		while(iterator.hasNext()) {
			Entry<String, FilterAttribute> entry = iterator.next();
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}
	}

}
