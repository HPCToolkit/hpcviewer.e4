package edu.rice.cs.hpcfilter.dialog;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcfilter.AbstractFilterPane;
import edu.rice.cs.hpcfilter.BaseFilterPane;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.FilterInputData;
import edu.rice.cs.hpcfilter.StringFilterDataItem;


/******************************************************
 * 
 * Dialog window specifically to filter ranks or threads or id tuples
 *
 ******************************************************/
public class ThreadFilterDialog extends TitleAreaDialog 
{
	private final FilterInputData data;
	private BaseFilterPane filterPane;
	
	
	public ThreadFilterDialog(Shell parentShell, 
							  List<FilterDataItem> items) {
		super(parentShell);		
		data = new FilterInputData(items);
	}
	

	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(600, 500);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
	    Composite composite = (Composite) super.createDialogArea(parent);

	    final String TITLE = "Select threads to view";
		final String MSGS  = "Please check any threads to be viewed.";
		
		setTitle( TITLE);
		getShell().setText( TITLE);
		setMessage(MSGS);
		
		filterPane = new BaseFilterPane(composite, AbstractFilterPane.STYLE_INDEPENDENT, data) {

			@Override
			protected String[] getColumnHeaderLabels() {
				final String []LABELS = {"Visible", "Threads"};
				return LABELS;
			}

		};

		return composite;
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
	
	public List<FilterDataItem> getResult() {
		return filterPane.getEventList();
	}
}
