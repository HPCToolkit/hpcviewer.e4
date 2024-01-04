package edu.rice.cs.hpcfilter.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
	private final String title;

	private BaseFilterPane<String> filterPane;
	
	public ThreadFilterDialog(Shell parentShell, String title,
							  List<FilterDataItem<String>> items) {
		super(parentShell);		
		data = new FilterInputData<>(items);
		this.title = title;
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
		getShell().setText( title);
		
		Composite composite = new Composite(parent, SWT.BORDER);

		GridLayout grid = new GridLayout();
		grid.numColumns=1;
		// bad hack: Have to add a "pad" margin on the top
		// This may be a SWT bug that the position of the composite is negative on Mac
		int padding = 0;
		if (OSValidator.isMac()) 
			padding = 30;
		
		grid.marginTop=padding;

		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(grid);

		filterPane = getFilterPane(composite);
		
		return composite;
	}
	
	
	/****
	 * Method to get the main filter pane.
	 * It can be override by a subclass
	 *  
	 * @param composite
	 * 			The container of the filter
	 * @return
	 */
	protected BaseFilterPane<String> getFilterPane(Composite composite) {
		return new BaseFilterPane<String>(composite, AbstractFilterPane.STYLE_INDEPENDENT, data) {

			@Override
			protected String[] getColumnHeaderLabels() {
				return new String[] {"  ", "Execution contexts"};
			}
			
			@Override
			public void changeEvent(Object data) {
				// make sure the OK button is only enabled when at least one item is checked
				var list = getEventList();
				if (list != null && !list.isEmpty())
					// traverse the list in O(n) fashion to see if at least there
					for(var item: list) {
						if (item.checked) {
							// an item has been checked, enabled the ok button
							getButton(IDialogConstants.OK_ID).setEnabled(true);
							return;
						}
					}
				getButton(IDialogConstants.OK_ID).setEnabled(false);
			}
		};
	}

	
	/****
	 * Show a filter dialog window and return the list of included items
	 * @param shell parent shell
	 * @param labels array of labels
	 * @param checked array of status of the labels: true if the label is checked
	 * @return {@code List<FilterDataItem>} or null if the user click "Cancel" button.
	 */
	public static List<FilterDataItem<String>> filter(Shell shell, String title, Object[] labels, boolean []checked) {

		List<FilterDataItem<String>> items =  new ArrayList<>(labels.length);
		
		for (int i=0; i<labels.length; i++) {
			boolean isChecked = (checked != null) && checked[i];
			FilterDataItem<String> obj = new StringFilterDataItem((String) labels[i], isChecked, true);
			items.add(obj);
		}

		ThreadFilterDialog dialog = new ThreadFilterDialog(shell, title, items);
		if (dialog.open() == Window.OK) {
			return dialog.getResult();
		}
		return Collections.emptyList();
	}
	
	public List<FilterDataItem<String>> getResult() {
		return filterPane.getEventList();
	}
}
