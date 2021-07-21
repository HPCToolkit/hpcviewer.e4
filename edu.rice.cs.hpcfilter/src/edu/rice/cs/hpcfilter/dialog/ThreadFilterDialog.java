package edu.rice.cs.hpcfilter.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.StringFilterDataItem;


/******************************************************
 * 
 * Dialog window specifically to filter ranks or threads or id tuples
 *
 ******************************************************/
public class ThreadFilterDialog extends AbstractFilterDialog 
{
	public ThreadFilterDialog(Shell parentShell, List<FilterDataItem> items) {
		this(parentShell, items, null);
	}
	
	public ThreadFilterDialog(Shell parentShell, List<FilterDataItem> items, List<Short> listIdTupleKinds) {
		super(parentShell, "Select threads to view", 
				"Please check any threads to be viewed.\nYou can narrow the list by specifying partial name of the threads on the filter.", 
				items);
	}
	
	/****
	 * Show a filter dialog window and return the list of included items
	 * @param shell parent shell
	 * @param labels array of labels
	 * @param checked array of status of the labels: true if the label is checked
	 * @return {@code List<FilterDataItem>} or null if the user click "Cancel" button.
	 */
	public static List<FilterDataItem> filter(Shell shell, String[] labels, boolean []checked) {

		List<FilterDataItem> items =  new ArrayList<FilterDataItem>(labels.length);
		
		for (int i=0; i<labels.length; i++) {
			boolean isChecked = checked == null? false : checked[i];
			FilterDataItem obj = new StringFilterDataItem(labels[i], isChecked, true);
			items.add(obj);
		}

		ThreadFilterDialog dialog = new ThreadFilterDialog(shell, items);
		if (dialog.open() == Window.OK) {
			return dialog.getResult();
		}
		return null;
	}

	
	@Override
	protected void createAdditionalFilter(Composite parent) {}

	@Override
	protected void createAdditionalButton(Composite parent) {}
	
	
	// unit test
	
	static public void main(String argv[]) {
		Shell shell = new Shell();
		List<FilterDataItem> items = new ArrayList<FilterDataItem>();
		Random random = new Random();
		
		for(int i=0; i<20; i++) {

			int rank = random.nextInt(10);
			int thread = random.nextInt(100);
			String label = IdTupleType.kindStr(IdTupleType.KIND_RANK) + " " + rank + " " +
					   	   IdTupleType.kindStr(IdTupleType.KIND_THREAD) + " " + thread;
			
			FilterDataItem obj = new StringFilterDataItem(label, i<6, i>3);
			items.add(obj);
		}
		
		List<Short> listKinds = new ArrayList<Short>();
		listKinds.add(IdTupleType.KIND_RANK);
		listKinds.add(IdTupleType.KIND_THREAD);	
		
		ThreadFilterDialog dialog = new ThreadFilterDialog(shell, items, listKinds);
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
