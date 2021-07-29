package edu.rice.cs.hpcfilter.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.StringFilterDataItem;
import edu.rice.cs.hpcfilter.dialog.ThreadFilterDialog;

public class ThreadFilterDialogTest {

	public static void main(String[] args) {
		Shell shell = new Shell();
		List<FilterDataItem> items = new ArrayList<FilterDataItem>();
		Random random = new Random();
		
		for(int i=0; i<20; i++) {

			int rank = random.nextInt(10);
			int thread = random.nextInt(100);
			String label = IdTupleType.LABEL_RANK   + " " + rank + " " +
					   	   IdTupleType.LABEL_THREAD + " " + thread;
			
			FilterDataItem obj = new StringFilterDataItem(label, i<6, i>3);
			items.add(obj);
		}
		
		ThreadFilterDialog dialog = new ThreadFilterDialog(shell, items);
		if (dialog.open() == Dialog.OK) {
			System.out.println("result-ok: " + dialog.getReturnCode());
			items = dialog.getResult();
			
			int i=0;
			for(FilterDataItem res : items) {
				System.out.println("\t" + i + ": " + res.data + " -> " + res.checked);
				i++;
			}
		}
	}

}
