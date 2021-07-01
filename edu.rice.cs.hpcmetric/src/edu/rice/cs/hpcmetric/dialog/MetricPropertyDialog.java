package edu.rice.cs.hpcviewer.ui.dialogs;

import java.util.ArrayList;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.Metric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.VisibilityType;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.util.string.StringUtil;



/*****
 * 
 * dialog window class to show list of metrics 
 *
 */
public class MetricPropertyDialog extends TitleAreaDialog 
{
	private TableViewer viewer;
	private Button btnEdit;

	/** The selected experiment **/
	private final Experiment experiment; 

	/***
	 * Default constructor: 
	 *  <p/>
	 *  There is no return value of this window. Each caller is
	 *  responsible to check the metrics if they are modified or not
	 * 
	 * @param parentShell : the parent shell of this dialog
	 * @param window : the window where the database is stored.
	 * 	in hpcviewer, list of databases is managed based on window
	 *  if the value of window is null, then users have to use the
	 *  method setELements() to setup the list of metrics to modify
	 */
	public MetricPropertyDialog(Shell parentShell, Experiment experiment) {
		super(parentShell);
		this.experiment = experiment;
	}

	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		
		Control contents = super.createContents(parent);
		
		final String TITLE = "Metric property";
		
		setTitle(TITLE);
		getShell().setText(TITLE);
		
		setMessage("Double-click the cell or select a metric and click edit button to modify the metric");
		
		return contents;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite aParent) {
		
		// initialize table viewer in the derived class
		initTableViewer(aParent);
		
		return aParent;
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
		
		Control ctrl = super.createButtonBar(parent);

		// -----------------
		// edit button: use the default "details" button ID
		// -----------------
		
		btnEdit = createButton((Composite) ctrl, IDialogConstants.DETAILS_ID, "Edit", true);
		btnEdit.setEnabled(false);		
		btnEdit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {				
				doAction();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
			
		});

		return ctrl;
	}

	//--------------------------------------------------
	//	PRIVATE
	//--------------------------------------------------

	/***
	 * initialize the table
	 * 
	 * @param composite
	 */
	private void initTableViewer(Composite composite) {
		
				
		// -----------------
		// metrics table 
		// -----------------
		
		Table table = new Table(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		table.setHeaderVisible(true);

		viewer = new TableViewer(table);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				
				boolean isEnabled = (getSelectElement() != null);

				// Eclipse bug (or feature?): case of no metric is selected
				// On Mac, a SelectionChangedEvent is triggered when we refresh the input
				// 	in this case, no item has been selected since the content of the table is new
				btnEdit.setEnabled(isEnabled);
			}
		});
		
		// set double click listener
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				doAction();
			}
		});
		
		// set the provider to handle the table content
		viewer.setContentProvider( new ArrayContentProvider() );
		
		// first column: metric name
		final TableViewerColumn columnName = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn colName = columnName.getColumn();
		colName.setText("Metric");
		colName.setWidth(200);
		columnName.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element) {
				final PropertiesModel obj = (PropertiesModel) element;
				return obj.sTitle;
			}
			
			@Override
			public String getToolTipText(Object element) {
				final PropertiesModel obj = (PropertiesModel) element;
				return obj.sTitle;
			}
		});
		
		// second column: description
		final TableViewerColumn columnDesc = new TableViewerColumn(viewer, SWT.NONE | SWT.WRAP);
		final TableColumn colDesc = columnDesc.getColumn();
		colDesc.setText("Description");
		colDesc.setWidth(100);
		columnDesc.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element) {
				final PropertiesModel obj = (PropertiesModel) element;
				// wrap the description into 70 characters. This number is completely heuristic
				// and may not fit properly in the column if using different font
				return StringUtil.wrapScopeName(obj.metric.getDescription(), 70);
			}
			
			@Override
			public String getToolTipText(Object element) {
				final PropertiesModel obj = (PropertiesModel) element;
				final String description  = StringUtil.wrapScopeName(obj.metric.getDescription(), 100);
				return description;
			}
		});
		
		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);
		
		GridDataFactory.defaultsFor(table).hint(600, 300).grab(true, true).applyTo(table);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(table);
		
		setElements();
	}


	
	/***********
	 * create an array for the input of the table
	 * 
	 * @param exp : experiment database 
	 */
	private ArrayList<PropertiesModel> createInput() {
		
		final ArrayList<PropertiesModel> arrElements = new ArrayList<PropertiesModel>();
		
		for(BaseMetric metric: experiment.getVisibleMetrics()) {
			
			// we don't want to show empty metric (if exist) and 
			// metric with invisible type
			
			if (metric != null) {

				String sTitle = metric.getDisplayName();
				
				PropertiesModel model = new PropertiesModel(sTitle, metric);
				arrElements.add( model );
			}
		}
		return arrElements;
	}
	
	/***
	 * set the value for arrElements (used by table) based on the specified experiment
	 * 
	 * @param exp
	 */
	private void setElements() {
		
		if (experiment == null)
			return;
		
		final ArrayList<PropertiesModel> arrElements = createInput();
		viewer.setInput(arrElements);
		
		// adjust the width of the columns
		// use pack() to let Eclipse SWT to decide how to arrange the widths.
		// it seems more portable to most platforms than using column weight
		
		final TableColumn []columns = viewer.getTable().getColumns();
		if (columns == null)
			return;
		
		for (TableColumn col : columns) {
			col.pack();
		}
	}
	

	/***
	 * retrieve the selected object in the table
	 * 
	 * @return The selected PropertiesModel element
	 */
	private PropertiesModel getSelectElement() {
		
		ISelection selection = viewer.getSelection();
		PropertiesModel obj = (PropertiesModel) ((StructuredSelection) selection).getFirstElement();
		
		return obj;
	}
	
	/****
	 * show the dialog window
	 */
	private void doAction() {
		PropertiesModel obj = getSelectElement();
		BaseMetric metric = obj.metric;
		
		if (metric == null)
			return;
		
		if (metric instanceof DerivedMetric) {
			// TODO: hack fix: we need to get any scope in the tree.
			// to get the first root if the safest we can have.
			RootScope root = (RootScope) experiment.getRootScopeChildren()[0];
			ExtDerivedMetricDlg dialog = new ExtDerivedMetricDlg( getShell(), 
					experiment,	root );
			
			DerivedMetric dm = (DerivedMetric) metric;
			dialog.setMetric(dm);
			
			if (dialog.open() == Dialog.OK) {
				
				dm = dialog.getMetric();
				
				updateMetricName(dm, dm.getDisplayName() );				
			}
			
		} else {
			InputDialog inDlg = new InputDialog(getShell(), "Edit metric display name", 
					"Enter the new display name", metric.getDisplayName(), null);
			if (inDlg.open() == Dialog.OK) {
				String name = inDlg.getValue();
				updateMetricName(metric, name);
			}
		}
	}
	
	/***
	 * make change the metric
	 * 
	 * @param metric
	 * @param sNewName
	 */
	private void updateMetricName(BaseMetric metric, String sNewName) {
		
		PropertiesModel obj = getSelectElement();
		obj.sTitle = sNewName;
		metric.setDisplayName(sNewName);
		viewer.update(obj, null);
	}

	
	/* (non-Javadoc)
	* @see org.eclipse.jface.window.Window#setShellStyle(int)
	*/
	protected void setShellStyle(int newShellStyle) {

		super.setShellStyle(newShellStyle | SWT.RESIZE | SWT.MAX);
	}
	
	//--------------------------------------------------
	//	CLASSES
	//--------------------------------------------------

	/**
	 * Data model for the column properties
	 * Containing two items: the state and the title
	 *
	 */
	protected class PropertiesModel {

		public String sTitle;
		public BaseMetric metric;

		public PropertiesModel(String s, BaseMetric metric) {
			this.sTitle = s;
			this.metric = metric;
		}
	}

	
	//--------------------------------------------------
	//	Unit test
	//--------------------------------------------------

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		
		// first step: initialize the column, make sure they have all the data to 
		//	distinguish with user custom column
		Experiment exp = new Experiment();
		java.util.List<BaseMetric> list = new java.util.ArrayList<BaseMetric>(10);
		
		for (int i=0; i<10; i++) {
			final String id = String.valueOf(4 * i + 10);
			list.add( new Metric(id, id + ": this is a long description of the metric. Everyone knows that there is no such as thing as a long description. But for the sake sanity test (and insanity test), we do this stupid test on purpose. Please ignore any stupidity in this code.", 
					"M" + id, VisibilityType.SHOW, null, null, null, i, MetricType.INCLUSIVE, i) );
		}
		exp.setMetrics(list);
		
		// second step: create the dialog, and implement all the abstract interfaces
		MetricPropertyDialog dialog = new MetricPropertyDialog(shell, exp);
		
		dialog.open();
	}

}
