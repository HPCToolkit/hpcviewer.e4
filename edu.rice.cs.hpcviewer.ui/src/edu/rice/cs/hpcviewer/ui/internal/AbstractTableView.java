package edu.rice.cs.hpcviewer.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import edu.rice.cs.hpcbase.ViewerDataEvent;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcmetric.MetricDataEvent;
import edu.rice.cs.hpcmetric.MetricFilterInput;
import edu.rice.cs.hpcmetric.internal.MetricFilterDataItem;
import edu.rice.cs.hpctree.ScopeTreeTable;
import edu.rice.cs.hpctree.action.HotPathAction;
import edu.rice.cs.hpctree.action.ZoomAction;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.actions.UserDerivedMetric;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.resources.IconManager;



public abstract class AbstractTableView extends AbstractView implements EventHandler
{
	final private int ACTION_ZOOM_IN      = 0;
	final private int ACTION_ZOOM_OUT     = 1;
	final private int ACTION_HOTPATH      = 2;
	final private int ACTION_ADD_METRIC   = 3;
	final private int ACTION_EXPORT_DATA  = 4;
	final private int ACTION_COLUMN_HIDE  = 5; 
	private Composite    parent ;
	private ToolItem     toolItem[];
	private LabelMessage lblMessage;
	
	private IMetricManager metricManager;
	private RootScope      root;
	private ScopeTreeTable table ;
	private ZoomAction     zoomAction;

	private ProfilePart profilePart;
	private IEventBroker eventBroker;

	public AbstractTableView(CTabFolder parent, int style, String title) {
		super(parent, style);
		setText(title);
	}


	@Override
	public void setService(EPartService partService, IEventBroker broker, DatabaseCollection database,
						   ProfilePart profilePart, EMenuService menuService) {

		this.profilePart = profilePart;
		this.eventBroker = broker;
	}

	
	/**
	 * finalize tool item and add it to the current toolbar
	 * 
	 * @param toolbar parent toolbar
	 * @param toolbarStyle style of toolitem ex: {@link SWT.PUSH}
	 * @param name the name of the image file
	 * @param tooltip the tooltip text
	 * 
	 * @return {@code ToolItem} created tool item
	 */
	protected ToolItem createToolItem(ToolBar toolbar, int toolbarStyle, String name, String tooltip) {
		
		IconManager iconManager = IconManager.getInstance();
		ToolItem toolitem = new ToolItem(toolbar, toolbarStyle);
		
		toolitem.setImage(iconManager.getImage(name));
		toolitem.setToolTipText(tooltip);
		toolitem.setEnabled(false);
		
		return toolitem;
	}

	/**
	 * finalize tool item and add it to the current toolbar with {@link SWT.PUSH} as the style
	 * 
	 * @param toolbar parent toolbar
	 * @param name the name of the image file
	 * @param tooltip the tooltip text
	 * @return {@code ToolItem}
	 */
	protected ToolItem createToolItem(ToolBar toolbar, String name, String tooltip) {
		if (name == null) {
			return new ToolItem(toolbar, SWT.SEPARATOR);
		}
		return createToolItem(toolbar, SWT.PUSH, name, tooltip);
	}
	
    /***
     * create cool item based on given toolbar
     * 
     * @param coolBar parent cool bar
     * @param toolBar toolbar to be added in the cool item
     */
	protected void createCoolItem(CoolBar coolBar, ToolBar toolBar) {

		CoolItem coolItem = new CoolItem(coolBar, SWT.NONE);
		coolItem.setControl(toolBar);
		Point size = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		size.x += 20;
		coolItem.setSize(size);
    	
    }
	
	

	@Override
	public void createContent(Composite parent) {
		this.parent = parent;
		parent.setLayout(new GridLayout(1, false));
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite composite = new Composite(parent, SWT.BORDER);
		composite.setLayout(new GridLayout(2, false));
		GridData gd_composite = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_composite.widthHint = 506;
		composite.setLayoutData(gd_composite);
				
		CoolBar coolBar = new CoolBar(composite, SWT.FLAT);
		ToolBar toolBar = new ToolBar(coolBar, SWT.FLAT | SWT.RIGHT);

		// -------------------------------------------
		// add the beginning of toolbar
		// -------------------------------------------
		beginToolbar(coolBar, toolBar);
		
		// -------------------------------------------
		// default tool bar
		// -------------------------------------------
		toolItem = new ToolItem[6];
		
		toolItem[ACTION_ZOOM_IN]  = createToolItem(toolBar, IconManager.Image_ZoomIn,  "Zoom-in the selected node");
		toolItem[ACTION_ZOOM_OUT] = createToolItem(toolBar, IconManager.Image_ZoomOut, "Zoom-out from the current tree scope");
		toolItem[ACTION_HOTPATH]  = createToolItem(toolBar, IconManager.Image_FlameIcon, "Expand the hot path below the selected node");
		
		toolItem[ACTION_ADD_METRIC]  = createToolItem(toolBar, IconManager.Image_FnMetric, "Create a new user-derived metric");
		toolItem[ACTION_EXPORT_DATA] = createToolItem(toolBar, IconManager.Image_SaveCSV,  "Export the current table into a CSV file");
		toolItem[ACTION_COLUMN_HIDE] = createToolItem(toolBar, IconManager.Image_CheckColumns,  "Show/hide columns");

		
		// -------------------------------------------
		// add the end of toolbar
		// -------------------------------------------
		endToolbar(coolBar, toolBar);

		createCoolItem(coolBar, toolBar);
		
		Point p = coolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		p.x += 20;
		
		coolBar.setSize(p);
		
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(coolBar);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(coolBar);

		// -------------------------------------------
		// message label
		// -------------------------------------------

		lblMessage = new LabelMessage(composite, SWT.NONE);
	}

	@Override
	public void setInput(Object input) {
		if (!(input instanceof IMetricManager))
			return;
		
		// -------------------------------------------
		// table creation
		// -------------------------------------------
		
		metricManager = (IMetricManager) input;
		root = ((Experiment)metricManager).getRootScope(RootScopeType.CallingContextTree);
		
		table = new ScopeTreeTable(parent, SWT.NONE, root, metricManager);
		table.pack();
		table.addSelectionListener(scope -> updateButtonStatus());
		
		table.addActionListener(scope -> {
			profilePart.addEditor(scope);
		});
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(table);
		
		updateButtonStatus();

		addButtonListener(root, metricManager);

		eventBroker.subscribe(ViewerDataEvent.TOPIC_HPC_ADD_NEW_METRIC, this);
		eventBroker.subscribe(ViewerDataEvent.TOPIC_HIDE_SHOW_COLUMN,   this);
	}

	@Override
	public Object getInput() {
		return metricManager;
	}

	@Override
	public List<FilterDataItem<BaseMetric>> getFilterDataItems() {
		final List<BaseMetric> visibleMetrics = getMetricManager().getVisibleMetrics();
		final List<FilterDataItem<BaseMetric>> list = new ArrayList<FilterDataItem<BaseMetric>>(visibleMetrics.size());
		final int []hiddenColumnIndexes = table.getHiddenColumnIndexes();
		
		for(int i=0; i<visibleMetrics.size(); i++) {
			final BaseMetric metric = visibleMetrics.get(i);
			boolean checked = true;
			
			// FIXME: ugly code. Try to find if this metric is hidden or not by
			//        checking if the metric order is the hidden column indexes
			for (int j=0; j<hiddenColumnIndexes.length; j++) {
				if (hiddenColumnIndexes[j]+1 == i) {
					checked = false;
					break;
				}
			}
			final MetricValue mv = root.getMetricValue(metric);
			final boolean enabled = mv != MetricValue.NONE;
			if (!enabled) checked = false;
			
			FilterDataItem<BaseMetric> item = new MetricFilterDataItem(metric, checked, enabled);
			list.add(item);
		}
		return list;
	}
	

	@Override
	public IMetricManager getMetricManager() {
		return metricManager;
	}

	@Override
	public void activate() {
		if (table != null && !table.isDisposed())
			table.redraw();
	}

	
	@Override
	public void handleEvent(Event event) {
		if (table == null || table.isDisposed()) {
			eventBroker.unsubscribe(this);
			return;
		}
		Object obj = event.getProperty(IEventBroker.DATA);
		if (obj == null || metricManager == null)
			return;
		
		if (!(obj instanceof ViewerDataEvent)) 
			return;
		
		ViewerDataEvent eventInfo = (ViewerDataEvent) obj;
		if (metricManager != eventInfo.experiment) 
			return;

		String topic = event.getTopic();
		if (topic.equals(ViewerDataEvent.TOPIC_HIDE_SHOW_COLUMN)) {
			
			MetricDataEvent dataEvent = (MetricDataEvent) eventInfo.data;
			
			// if the hide/show event is only for the current active view,
			// we need to check if this one if the active or not
			// if not, just leave it
			
			if (!dataEvent.isApplyToAll()) {
				if (profilePart.getActiveView() != this)
					return;
			}
			hideORShowColumns(dataEvent);
		}
	}
	
	
	private void hideORShowColumns(MetricDataEvent dataEvent) {
		Object objData = dataEvent.getData();
		
		// two possibilities of data types for this event:
		// - List: it contains the list of columns to be shown/hidden
		// - MetricFilterDataItem: a column to be shown/hidden 
		
		if (objData instanceof List<?>) {
			@SuppressWarnings("unchecked")
			final List<MetricFilterDataItem> list = (List<MetricFilterDataItem>) objData;
			final List<BaseMetric> metrics = getMetricManager().getVisibleMetrics();
			
			// create the list of column status: true if shown, false if hidden
			for(MetricFilterDataItem item: list) {
				if (!item.enabled)
					continue;
				
				int i = getColumnIndexByMetric(item, metrics);
				hideOrShowColumn(i, item.checked);
			}
			
		} else if (objData instanceof MetricFilterDataItem) {
			MetricFilterDataItem item = (MetricFilterDataItem) objData; 
			List<BaseMetric> metrics = getMetricManager().getVisibleMetrics();
			
			int index = getColumnIndexByMetric(item, metrics);
			hideOrShowColumn(index, item.checked);
		}
	}
	
	private void hideOrShowColumn(int columnIndex, boolean shown) {
		// the index zero is only for the tree column
		// we want to keep this column to be always visible
		if (columnIndex == 0) 
			return;
		
		if (shown) {
			table.showColumn(columnIndex);
		} else {
			table.hideColumn(columnIndex);
		}
	}
	
	
	private int getColumnIndexByMetric(MetricFilterDataItem item, List<BaseMetric> metrics) {
		// index 0 is reserved for the tree column, and it's always shown
		int index=1;
		BaseMetric metric = item.data;
		
		for(BaseMetric m : metrics) {
			if (m == metric) {
				return index;
			}
			MetricValue mv = root.getMetricValue(m);
			if (mv != MetricValue.NONE)
				index++;
		}
		return -1;
	}
	
	
	private void addButtonListener(RootScope root, IMetricManager metricManager) {
		
		zoomAction = new ZoomAction(table);	

		toolItem[ACTION_ZOOM_IN].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				Scope selection = table.getSelection();
				if (selection == null)
					return;

				zoomAction.zoomIn(selection);
				updateButtonStatus();
			}
		});
		
		toolItem[ACTION_ZOOM_OUT].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				zoomAction.zoomOut();
				updateButtonStatus();
			}
		});
		
		toolItem[ACTION_HOTPATH].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				HotPathAction action = new HotPathAction(table);
				if (action.showHotCallPath() == HotPathAction.RET_ERR) {
					lblMessage.showErrorMessage(action.getMessage());
				}
			}
		});

		toolItem[ACTION_ADD_METRIC].addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				UserDerivedMetric derivedMetricAction = new UserDerivedMetric(root, metricManager, eventBroker);
				derivedMetricAction.addNewMeric();
			}			
		});
		toolItem[ACTION_COLUMN_HIDE].addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				MetricFilterInput input = new MetricFilterInput(root, getMetricManager(), 
																getFilterDataItems(), true);
				profilePart.addEditor(input);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	}

	protected void updateButtonStatus() {
		updateStatus();
		
		if (table == null) {
			toolItem[ACTION_ZOOM_IN] .setEnabled(false);
			toolItem[ACTION_ZOOM_OUT].setEnabled(false);
			toolItem[ACTION_HOTPATH] .setEnabled(false);
			return;
		}
		toolItem[ACTION_ADD_METRIC] .setEnabled(true);
		toolItem[ACTION_COLUMN_HIDE].setEnabled(true);
		
		Scope selectedScope = table.getSelection();
		boolean canZoomIn = zoomAction == null ? false : zoomAction.canZoomIn(selectedScope); 
		toolItem[ACTION_ZOOM_IN].setEnabled(canZoomIn);
		
		boolean canZoomOut = zoomAction == null ? false : zoomAction.canZoomOut();
		toolItem[ACTION_ZOOM_OUT].setEnabled(canZoomOut);
		
		boolean canHotPath = table.getSelection() != null;
		toolItem[ACTION_HOTPATH].setEnabled(canHotPath);
	}
	
    protected abstract void beginToolbar(CoolBar coolbar, ToolBar toolbar);
    protected abstract void endToolbar  (CoolBar coolbar, ToolBar toolbar);
    protected abstract void updateStatus();
}
