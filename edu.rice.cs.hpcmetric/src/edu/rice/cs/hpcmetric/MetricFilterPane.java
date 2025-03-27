// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcmetric;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.prefs.Preferences;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import edu.rice.cs.hpcbase.BaseConstants.ViewType;
import edu.rice.cs.hpcbase.ViewerDataEvent;
import edu.rice.cs.hpcbase.map.UserInputHistory;
import org.hpctoolkit.db.local.experiment.metric.BaseMetric;
import org.hpctoolkit.db.local.experiment.metric.DerivedMetric;
import org.hpctoolkit.db.local.experiment.metric.IMetricManager;
import edu.rice.cs.hpcfilter.AbstractFilterPane;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.FilterDataItemSortModel;
import edu.rice.cs.hpcfilter.FilterDataProvider;
import edu.rice.cs.hpcfilter.FilterInputData;
import edu.rice.cs.hpcfilter.IFilterChangeListener;
import edu.rice.cs.hpcmetric.dialog.ExtDerivedMetricDlg;
import edu.rice.cs.hpcmetric.internal.IConstants;
import edu.rice.cs.hpcmetric.internal.MetricFilterDataItem;
import edu.rice.cs.hpcmetric.internal.MetricFilterDataProvider;
import edu.rice.cs.hpcmetric.internal.MetricFilterSortModel;
import edu.rice.cs.hpcmetric.internal.MetricPainterConfiguration;
import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;


/************************************************************************
 * 
 * Special Inherited class from {@link AbstractFilterPane} for metrics.
 *
 ************************************************************************/
public class MetricFilterPane extends AbstractFilterPane<BaseMetric> 
	implements IFilterChangeListener, EventHandler, PropertyChangeListener
{	
	private static final String HISTORY_COLUMN_PROPERTY = "column_property";
	private static final String HISTORY_APPLY_ALL = "apply-all";

	private final IEventBroker eventBroker;

	private MetricPainterConfiguration painterConfiguration;
	private MetricFilterDataProvider dataProvider;
	private MetricFilterInput  input;
	
	private Button btnEdit;
	private Button btnApplyToAllViews;
	
	
	/*****
	 * Constructor to create filter for metric panel which includes button area and filter text.
	 * 
	 * @param parent
	 * 			The composite parent for the table
	 * @param style
	 * 			Supported style (or mode): {@link AbstractFilterPane.STYPE_COMPOSITE} and {@link AbstractFilterPane.STYLE_INDEPENDENT}
	 * @param eventBroker
	 * 			Eclipse event manager to broadcast any changes in the metric
	 * @param inputData
	 * 			{@code MetricFilterInput} the input data (possible modification)
	 */
	public MetricFilterPane(Composite parent, int style, IEventBroker eventBroker, MetricFilterInput inputData) {
		super(parent, style, inputData);
		this.input = inputData;
		this.eventBroker = eventBroker;
		
		if (style == STYLE_COMPOSITE) {
			// Real application, not within a simple unit test
			updateMetricManager(input.getMetricManager());
			eventBroker.subscribe(ViewerDataEvent.TOPIC_FILTER_POST_PROCESSING, this);
		}
	}


	/*****
	 * Change the input of the table with the new one.
	 * This method will completely wipe out the existing list items.
	 * 
	 * @param inputData
	 */
	public void setInput(FilterInputData<BaseMetric> inputData) { 
		// first, remove the listener to the old metric manager
		var metricManager = input.getMetricManager();
		
		// fix issue #321: some time the metric manager is null because it's the 
		// thread view is closed by the user
		if (metricManager != null)
			metricManager.removeMetricListener(this);

		// then register to listen to this manager
		input = (MetricFilterInput) inputData;

		updateMetricManager(input.getMetricManager());
		
		btnApplyToAllViews.setEnabled(input.getView().getViewType() == ViewType.COLLECTIVE);
		
		boolean selected = getHistoryApplyAll() && input.isAffectAll();
		btnApplyToAllViews.setSelection(selected);
		
		// important: has to reset the data provider to null 
		// so we'll create a different instance of data provider 
		dataProvider = null;
		super.reset(inputData);
		getNatTable().refresh();
		
		metricManager.addMetricListener(this);
	}
	
	
	private void updateMetricManager(IMetricManager metricManager) {		
		metricManager.addMetricListener(this);
	}
	
	@Override
	protected String getFilterLabel() {
		return "Metric's name to filter: ";
	}
	
	@Override
	public void changeEvent(Object data) {
		broadcast(data);
	}

	
	@Override
	protected int createAdditionalButton(Composite parent, FilterInputData<BaseMetric> inputData) {
		input = (MetricFilterInput) inputData; 
		
		btnApplyToAllViews = new Button(parent, SWT.CHECK);
		btnApplyToAllViews.setText("Apply to all views");
		btnApplyToAllViews.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		btnApplyToAllViews.setEnabled(input.getView().getViewType() == ViewType.COLLECTIVE);
		
		boolean checked = getHistoryApplyAll() && input.isAffectAll();
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
		// number of additional buttons: 2
		return 2;
	}
	
	
	@Override
	public void pack() {
		final String TEXT = "|/'_{]";

		GC gc = new GC(getNatTable());
		gc.setFont(FontManager.getMetricFont());
		Point sizeMetricFont = gc.textExtent(TEXT);
		
		gc.setFont(FontManager.getFontGeneric());
		Point sizeGenericFont = gc.textExtent(TEXT);
		
		gc.dispose();
		
		int height = 4 + Math.max(sizeMetricFont.y, sizeGenericFont.y);
		DataLayer dataLayer = getDataLayer();
		dataLayer.setDefaultRowHeight(height);
	}
	
	
	private void broadcast(Object data) {
		List<FilterDataItem<BaseMetric>> copyList = new ArrayList<>(getEventList());
		MetricDataEvent metricDataEvent = new MetricDataEvent(data, copyList, btnApplyToAllViews.getSelection());
		ViewerDataEvent viewerDataEvent = new ViewerDataEvent(input.getMetricManager(), metricDataEvent);
		
		if (eventBroker != null)
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
		Optional<FilterDataItem<BaseMetric>> mfdi = getEventList().stream()
												.filter( item -> item.data.getIndex() == metric.getIndex() )
												.findFirst();
		if (mfdi.isEmpty())
			return;
		
		FilterDataItem<BaseMetric> item = mfdi.get();
		item.data = metric;
		item.setLabel(metric.getDisplayName());
	}


	/****
	 * Edit a selected metric
	 * @param item {@code FilterDataItem} metric to be changed
	 */
	private void edit(FilterDataItem<BaseMetric> item) {
		if (!item.enabled)
			return;

		if (item.getData() instanceof DerivedMetric) {
			Shell shell = getNatTable().getShell();
			ExtDerivedMetricDlg dlg = new ExtDerivedMetricDlg(shell, 
					input.getMetricManager(), 
					input.getRoot());
			dlg.setMetric((DerivedMetric) item.getData());
			if (dlg.open() == Window.OK) {
				BaseMetric metric = dlg.getMetric();
				update(metric);
				
				ViewerDataEvent dataEvent = new ViewerDataEvent(input.getMetricManager(), metric);	
				eventBroker.post(ViewerDataEvent.TOPIC_HPC_METRIC_UPDATE, dataEvent);
			} 
		} else {
			Shell shell = getNatTable().getShell();
			BaseMetric metric = (BaseMetric) item.getData();

			InputDialog inDlg = new InputDialog(shell, "Edit metric display name", 
					"Enter the new display name", metric.getDisplayName(), null);
			if (inDlg.open() == Window.OK) {
				String name = inDlg.getValue();
				metric.setDisplayName(name);
				update(metric);
				
				ViewerDataEvent dataEvent = new ViewerDataEvent(input.getMetricManager(), metric);
				if (eventBroker != null)
					eventBroker.post(ViewerDataEvent.TOPIC_HPC_METRIC_UPDATE, dataEvent);
			}
		}
	}


	@Override
	protected void selectionEvent(FilterDataItem<BaseMetric> event, int action) {
		if (action == SWT.MouseDown) {
			btnEdit.setEnabled(event.enabled);
		} else if (action == SWT.MouseDoubleClick) {
			edit(event);
		}
	}


	@Override
	public void propertyChange(PropertyChangeEvent event) {
		final String property = event.getProperty();
		if (property.equals(PreferenceConstants.ID_FONT_METRIC) ||
			property.equals(PreferenceConstants.ID_FONT_GENERIC)) {

			painterConfiguration.configureRegistry(getNatTable().getConfigRegistry());
		}
		super.propertyChange(event);
		
		// need to refresh for the font metric because the parent
		// only refresh for changes in generic font
		if (property.equals(PreferenceConstants.ID_FONT_METRIC))
			getNatTable().refresh(false);
	}


	@Override
	protected String[] getColumnHeaderLabels() {
		return IConstants.COLUMN_LABELS;
	}

	
	@Override
	protected FilterDataProvider<BaseMetric> getDataProvider(FilterList<FilterDataItem<BaseMetric>> filterList) {
		if (dataProvider == null) {
			dataProvider = new MetricFilterDataProvider(input.getRoot(), filterList, this);
		} else {
			dataProvider.setList(filterList);
		}
		return dataProvider;
	}
	

	@Override
	protected FilterDataItemSortModel<BaseMetric> createSortModel(EventList<FilterDataItem<BaseMetric>> eventList) {
		return new MetricFilterSortModel(input.getRoot(), eventList);
	}

	
	@Override
	protected void addConfiguration(NatTable table) {
		painterConfiguration = new MetricPainterConfiguration();
		table.addConfiguration(painterConfiguration);		
		table.addDisposeListener(this);
	}


	@Override
	protected void setLayerConfiguration(DataLayer dataLayer) {
		dataLayer.setColumnWidthPercentageByPosition(0, 5);
		dataLayer.setColumnWidthPercentageByPosition(1, 25);
		dataLayer.setColumnWidthPercentageByPosition(2, 50);
		dataLayer.setColumnWidthPercentageByPosition(3, 20);
		dataLayer.setColumnsResizableByDefault(true);
	}


	@Override
	public void handleEvent(Event event) {
		Object obj = event.getProperty(IEventBroker.DATA);
		if (obj == null || input == null)
			return;
		
		// check if we have a generic event
		ViewerDataEvent eventInfo = (ViewerDataEvent) obj;
		if (input.getMetricManager() != eventInfo.metricManager) 
			return;

		if (event.getTopic().equals(ViewerDataEvent.TOPIC_FILTER_POST_PROCESSING)) {
			updateMetricManager(eventInfo.metricManager);
		}
	}


	@Override
	protected int createAdditionalFilter(Composite parent, FilterInputData<BaseMetric> inputData) {
		// No need to add filters
		return 0;
	}


	@Override
	public void propertyChange(java.beans.PropertyChangeEvent evt) {
		// something has changed in the list of metrics
		if (evt.getPropertyName().equals(org.hpctoolkit.db.local.event.EventList.PROPERTY_INSERT)) {
			// new metric has been added
			// need to refresh the underlying layer
			MetricFilterInput input2 = new MetricFilterInput(input.getView(), eventBroker);
			reset(input2);
		}
	}
}
