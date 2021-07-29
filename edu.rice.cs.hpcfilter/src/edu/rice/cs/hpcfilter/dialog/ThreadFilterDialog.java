package edu.rice.cs.hpcfilter.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcdata.util.OSValidator;
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
public class ThreadFilterDialog extends Dialog 
{
	private final FilterInputData<String> data;
	private BaseFilterPane<String> filterPane;
	
	
	public ThreadFilterDialog(Shell parentShell, 
							  List<FilterDataItem<String>> items) {
		super(parentShell);		
		data = new FilterInputData<String>(items);
	}
	

	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(600, 600);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
	    final String TITLE = "Select threads to view";
		
		getShell().setText( TITLE);
		
		Composite composite = new Composite(parent, SWT.BORDER);

		GridLayout grid = new GridLayout();
		grid.numColumns=1;
		// TODO: bad hack: Have to add a "pad" margin on the top
		// This may be a SWT bug that the position of the composite is negative on Mac
		int padding = 0;
		if (OSValidator.isMac()) 
			padding = 30;
		
		grid.marginTop=padding;

		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(grid);

		
		filterPane = new BaseFilterPane<String>(composite, AbstractFilterPane.STYLE_INDEPENDENT, data);

		return composite;
	}

	
	/****
	 * Show a filter dialog window and return the list of included items
	 * @param shell parent shell
	 * @param labels array of labels
	 * @param checked array of status of the labels: true if the label is checked
	 * @return {@code List<FilterDataItem>} or null if the user click "Cancel" button.
	 */
	public static List<FilterDataItem<String>> filter(Shell shell, String[] labels, boolean []checked) {

		List<FilterDataItem<String>> items =  new ArrayList<>(labels.length);
		
		for (int i=0; i<labels.length; i++) {
			boolean isChecked = checked == null? false : checked[i];
			FilterDataItem<String> obj = new StringFilterDataItem(labels[i], isChecked, true);
			items.add(obj);
		}

		ThreadFilterDialog dialog = new ThreadFilterDialog(shell, items);
		if (dialog.open() == Window.OK) {
			return dialog.getResult();
		}
		return null;
	}
	
	public List<FilterDataItem<String>> getResult() {
		return filterPane.getEventList();
	}
}
