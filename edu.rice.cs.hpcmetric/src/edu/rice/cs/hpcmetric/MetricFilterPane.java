package edu.rice.cs.hpcmetric;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.Preferences;

import edu.rice.cs.hpcbase.ViewerDataEvent;
import edu.rice.cs.hpcbase.map.UserInputHistory;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcfilter.AbstractFilterPane;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.FilterDataProvider;
import edu.rice.cs.hpcfilter.FilterInputData;
import edu.rice.cs.hpcmetric.dialog.ExtDerivedMetricDlg;
import edu.rice.cs.hpcmetric.internal.IConstants;
import edu.rice.cs.hpcmetric.internal.MetricFilterDataItem;
import edu.rice.cs.hpcmetric.internal.MetricFilterDataProvider;
import edu.rice.cs.hpcmetric.internal.MetricPainterConfiguration;
import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;


public class MetricFilterPane extends AbstractFilterPane 
{	
	private static final String HISTORY_COLUMN_PROPERTY = "column_property";
	private static final String HISTORY_APPLY_ALL = "apply-all";

	private final IEventBroker eventBroker;

	private Button btnEdit;
	private Button btnApplyToAllViews;
	private FilterDataProvider dataProvider;
	
	public MetricFilterPane(Composite parent, int style, IEventBroker eventBroker, FilterInputData inputData) {
		super(parent, style, inputData);
		this.eventBroker = eventBroker;
	}

	
	@Override
	public void changeEvent(Object data) {
		broadcast(data);
	}

	
	@Override
	protected void createAdditionalButton(Composite parent) {
		final MetricFilterInput  inputFilter = (MetricFilterInput) getInputData();
		
		btnApplyToAllViews = new Button(parent, SWT.CHECK);
		btnApplyToAllViews.setText("Apply to all views");
		btnApplyToAllViews.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		btnApplyToAllViews.setEnabled(inputFilter.isAffectAll());
		boolean checked = getHistoryApplyAll() && inputFilter.isAffectAll();
		btnApplyToAllViews.setSelection( checked );
		
		btnApplyToAllViews.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// if we select to apply to all views, we should notify all the views
				// to reflect changes of the column hide/show
				boolean selected = btnApplyToAllViews.getSelection();
				if (selected) {
					broadcast(getEventList());
				}
				// make sure we store the current selection, to be reused for the next session
				// this isn't critical, but it's just nice to memorize the previous state
				
				Preferences pref = UserInputHistory.getPreference(HISTORY_COLUMN_PROPERTY);
				pref.putBoolean(HISTORY_APPLY_ALL, selected);
				UserInputHistory.setPreference(pref);
			}
		});
		btnEdit = WidgetFactory.button(SWT.PUSH).text("Edit").enabled(false).create(parent);
		btnEdit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelectionProvider selection = getSelectionProvider();
				if (selection != null && !selection.getSelection().isEmpty()) {
					IStructuredSelection sel = (IStructuredSelection) selection.getSelection();
					Object element = sel.getFirstElement();
					edit((MetricFilterDataItem) element);
				}
			}
		});
	}
	
	private void broadcast(Object data) {
		final MetricFilterInput  inputFilter = (MetricFilterInput) getInputData();

		List<FilterDataItem> copyList = new ArrayList<FilterDataItem>(getEventList()); //List.copyOf(getList());
		MetricDataEvent metricDataEvent = new MetricDataEvent(data, copyList, btnApplyToAllViews.getSelection());
		ViewerDataEvent viewerDataEvent = new ViewerDataEvent(inputFilter.getMetricManager(), metricDataEvent);
		
		eventBroker.post(ViewerDataEvent.TOPIC_HIDE_SHOW_COLUMN, viewerDataEvent);
	}

	
	/***
	 * get the user preference of "apply-all"
	 * @return
	 */
	private boolean getHistoryApplyAll() {
		return UserInputHistory.getPreference(HISTORY_COLUMN_PROPERTY).getBoolean(HISTORY_APPLY_ALL, true);
	}
	

	/***
	 * Update the content of a visible metric. 
	 * If a metric doesn't exist in the visible list (for instance the metric is not shown), we quit
	 * 
	 * @param metric
	 */
	private void update(BaseMetric metric) {
		Optional<FilterDataItem> mfdi = getEventList().stream()
												.filter( item -> ((BaseMetric)item.data).getIndex() == metric.getIndex() )
												.findFirst();
		if (mfdi.isEmpty())
			return;
		
		FilterDataItem item = mfdi.get();
		item.data = metric;
		item.setLabel(metric.getDisplayName());
	}


	/****
	 * Edit a selected metric
	 * @param item {@code FilterDataItem} metric to be changed
	 */
	private void edit(FilterDataItem item) {
		if (!item.enabled)
			return;

		final MetricFilterInput  inputFilter = (MetricFilterInput) getInputData();

		if (item.getData() instanceof DerivedMetric) {
			Shell shell = getNatTable().getShell();
			ExtDerivedMetricDlg dlg = new ExtDerivedMetricDlg(shell, 
															  inputFilter.getMetricManager(), 
															  inputFilter.getRoot());
			dlg.setMetric((DerivedMetric) item.getData());
			if (dlg.open() == Dialog.OK) {
				BaseMetric metric = dlg.getMetric();
				update(metric);
				
				ViewerDataEvent dataEvent = new ViewerDataEvent(inputFilter.getMetricManager(), metric);				
				eventBroker.post(ViewerDataEvent.TOPIC_HPC_METRIC_UPDATE, dataEvent);
			} 
		} else {
			Shell shell = getNatTable().getShell();
			BaseMetric metric = (BaseMetric) item.getData();

			InputDialog inDlg = new InputDialog(shell, "Edit metric display name", 
					"Enter the new display name", metric.getDisplayName(), null);
			if (inDlg.open() == Dialog.OK) {
				String name = inDlg.getValue();
				metric.setDisplayName(name);
				update(metric);
				
				ViewerDataEvent dataEvent = new ViewerDataEvent(inputFilter.getMetricManager(), metric);				
				eventBroker.post(ViewerDataEvent.TOPIC_HPC_METRIC_UPDATE, dataEvent);
			}
		}
	}


	@Override
	protected void selectionEvent(FilterDataItem event, int action) {
		if (action == SWT.MouseDown) {
			btnEdit.setEnabled(event.enabled);
		} else if (action == SWT.MouseDoubleClick) {
			edit(event);
		}
	}


	@Override
	public void propertyChange(PropertyChangeEvent event) {
		final String property = event.getProperty();
		if (property.equals(PreferenceConstants.ID_FONT_GENERIC)) {
			getNatTable().redraw();
		}
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {}


	@Override
	protected String[] getColumnHeaderLabels() {
		return IConstants.COLUMN_LABELS;
	}


	@Override
	protected FilterDataProvider getDataProvider() {
		if (dataProvider == null) {
			MetricFilterInput input = (MetricFilterInput) getInputData();
			this.dataProvider = new MetricFilterDataProvider(input.getRoot(), getFilterList(), this);
		}
		return dataProvider;
	}


	@Override
	protected void addConfiguration(NatTable table) {
		table.addConfiguration(new MetricPainterConfiguration());
	}


	@Override
	protected void setLayerConfiguration(DataLayer dataLayer) {
		dataLayer.setColumnWidthPercentageByPosition(0, 10);
		dataLayer.setColumnWidthPercentageByPosition(1, 30);
		dataLayer.setColumnWidthPercentageByPosition(2, 40);
		dataLayer.setColumnWidthPercentageByPosition(3, 20);
		dataLayer.setColumnsResizableByDefault(true);
	}
}
