package edu.rice.cs.hpctraceviewer.ui.dialog;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.data.db.IdTupleType;
import edu.rice.cs.hpc.data.experiment.extdata.IFilteredData;
import edu.rice.cs.hpc.data.trace.Filter;
import edu.rice.cs.hpc.data.trace.FilterSet;


/*****
 * 
 * Filter dialog to create/edit filter glob pattern of processes
 *
 */
public class FilterDialog extends TitleAreaDialog 
{
	private static final String KEY_PREFIX_FILTER = "filter.";
	
	private List list;
	private IFilteredData filterData;
	private Button btnRemove;
	private Button btnShow;

	/****
	 * constructor for displaying filter glob pattern
	 * @param parentShell
	 */
	public FilterDialog(Shell parentShell, IFilteredData filteredBaseData) {
		super(parentShell);
		filterData = filteredBaseData;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		Group grpMode = new Group(composite, SWT.NONE);
		grpMode.setText("Mode of filter");
		
		btnShow = new Button(grpMode, SWT.RADIO);
		btnShow.setText("To show");
		btnShow.setToolTipText("An option to show matching patterns");

		Button btnHide = new Button(grpMode, SWT.RADIO);
		btnHide.setText("To hide");
		btnHide.setToolTipText("An option to hide matching patterns");
		
		
		FilterSet filter = null;
		if (filterData != null) {
			filter = filterData.getFilter();
			
			if (filter != null && filter.isShownMode())
				btnShow.setSelection(true);
			else
				btnHide.setSelection(true);
		}
		
		Label lblMode = new Label(grpMode, SWT.LEFT | SWT.WRAP);
		lblMode.setText("Selecting the 'To show' radio button will show matching processes, " +
						 "while selecting the 'To hide' button \nwill hide them.");
		
		GridDataFactory.swtDefaults().span(2, 1).grab(true, false).applyTo(lblMode);
		
		GridDataFactory.swtDefaults().grab(true, false).applyTo(grpMode);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(grpMode);

		Group grpFilter = new Group(composite, SWT.NONE);
		grpFilter.setText("Filter");

		GridDataFactory.fillDefaults().grab(true, true).applyTo(grpFilter);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(grpFilter);

		Composite coButtons = new Composite(grpFilter, SWT.NONE);

		GridDataFactory.fillDefaults().grab(false, true).applyTo(coButtons);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(coButtons);
		
		Button btnAdd = new Button(coButtons, SWT.PUSH | SWT.FLAT);
		btnAdd.setText("Add");
		btnAdd.setToolTipText("Add a new filtering pattern");
		btnAdd.addSelectionListener( new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {

				String message;
				
				java.util.List<Short> listTypes = filterData.getIdTupleTypes();
				String []title     = new String[listTypes.size()];
				String []histories = new String[listTypes.size()];

				for(int i=0; i<listTypes.size(); i++) {
					String type  = IdTupleType.kindStr(listTypes.get(i));
					histories[i] = KEY_PREFIX_FILTER + type;
					title[i]     = type + ": ";
				}
				
				message = "Please type a pattern in the format minimum:maximum:stride.\n" + 
						"Any omitted or invalid sections will match as many processes \nor threads as possible.\n\n" +
						"For instance, 3:7:2 in the process box with the thread box empty \nwill match all threads of processes 3, 5, and 7.\n"+
						"1 in the thread box with the process box empty will match \nthread 1 of all processes.\n"+
						"1::2 in the process box and 2:4:2 in the thread box will match \n1.2, 1.4, 3.2, 3.4, 5.2 ...";

				MultiInputDialog dlg = new MultiInputDialog(getShell(), "Add a pattern", message, title, histories);
				
				if (dlg.open() == Dialog.OK) {
					String []values = dlg.getValue();
					list.add(Arrays.toString(values));
					checkButtons();
				}
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(btnAdd);
		//btnAdd.setLayoutData(new RowData(80,20));
		
		btnRemove = new Button(coButtons, SWT.PUSH | SWT.FLAT);
		btnRemove.setText("Remove");
		btnRemove.setToolTipText("Remove a selected filtering pattern");
		btnRemove.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int i = list.getSelectionCount();
				if (i > 0) {
					final String item = list.getSelection()[0];
					list.remove(item);
					
					checkButtons();
				}
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(btnRemove);
		
		final Button btnRemoveAll = new Button(coButtons, SWT.PUSH | SWT.FLAT);
		btnRemoveAll.setText("Remove all");
		btnRemoveAll.setToolTipText("Remove all filtering patterns");
		btnRemoveAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int count = list.getItemCount();
				if (count>0) {
					if (MessageDialog.openQuestion(getShell(), "Remove all patterns",
							"Are you sure to remove all " + count + " patterns ?")) {
						list.removeAll();
						
						checkButtons();
					}
				}
			}
		}) ;
		GridDataFactory.fillDefaults().grab(true, false).applyTo(btnRemoveAll);
		
		list = new List(grpFilter, SWT.SINGLE | SWT.V_SCROLL);
		list.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				checkButtons();
			}			
		});
		GridDataFactory.fillDefaults().grab(true, true).hint(40, 80).applyTo(list);
		
		this.setMessage("Add/remove glob patterns to filter displayed processes");
		this.setTitle("Filter patterns");
		
		// add pattern into the list
		if (filterData != null && filter != null && filter.getPatterns() != null) {
			for (Filter flt : filter.getPatterns()) {
				list.add(flt.toString());
			}
		}

		checkButtons();
		
		return parent;
	}
	
	
	private void checkButtons() {
		boolean selected = (list.getSelectionCount()>0);
		btnRemove.setEnabled(selected);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#setShellStyle(int)
	 */
	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(newShellStyle | SWT.RESIZE);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		java.util.List<Short> listTypes = filterData.getIdTupleTypes();
		ArrayList<Filter> filterList = new ArrayList<Filter>();

		for(int i=0; i<list.getItemCount(); i++) {
			String item = list.getItem(i);
			String []items = item.replace("[", "").replace("]", "").split(",");
			filterList.add(new Filter(listTypes, items));
		}
		FilterSet filterSet = filterData.getFilter();
		
		// put the glob pattern back
		filterSet.setPatterns(filterList);
		// set the show mode (to show or to hide)
		filterSet.setShowMode( btnShow.getSelection() );
		
		// check if the filter is correct
		filterData.setFilter(filterSet);
		if (filterData.isGoodFilter()) {
			super.okPressed();
		} else {
			// it is not allowed to filter everything
			MessageDialog.openError(getShell(), "Error", 
					"The result of filter is empty ranks.\nIt isn't allowed to filter all the ranks.");
		}
	}	
}


