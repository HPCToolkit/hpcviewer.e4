package edu.rice.cs.hpcviewer.ui.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

import edu.rice.cs.hpcbase.BaseConstants;
import edu.rice.cs.hpcbase.BaseConstants.ViewType;
import edu.rice.cs.hpcbase.ViewerDataEvent;
import edu.rice.cs.hpcbase.ui.IUserMessage;

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
import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpctree.IScopeTreeAction;
import edu.rice.cs.hpctree.IScopeTreeData;
import edu.rice.cs.hpctree.ScopeTreeTable;
import edu.rice.cs.hpctree.TableFitting;
import edu.rice.cs.hpctree.TableFitting.ColumnFittingMode;
import edu.rice.cs.hpctree.action.HotPathAction;
import edu.rice.cs.hpctree.action.IUndoableActionManager;
import edu.rice.cs.hpctree.action.UndoableActionManager;
import edu.rice.cs.hpctree.action.ZoomAction;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.actions.UserDerivedMetric;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.resources.IconManager;


/************************************************************************************************
 * 
 * Generic class to display a table and its toolbar for the actions
 *
 ************************************************************************************************/
public abstract class AbstractTableView extends AbstractView 
implements EventHandler, IUserMessage
{
	private static final String TOOLTIP_AUTOFIT = "Resize the width of metric columns. ";
	private static final String AUTOFIT_EVENT   = "eventautofit";
	
	private static final int ACTION_ZOOM_IN      = 0;
	private static final int ACTION_ZOOM_OUT     = 1;
	private static final int ACTION_HOTPATH      = 2;
	
	private static final int ACTION_ADD_METRIC   = 3;
	private static final int ACTION_EXPORT_DATA  = 4;
	private static final int ACTION_COLUMN_HIDE  = 5;
	
	private static final int ACTION_FONT_INC     = 6;
	private static final int ACTION_FONT_DEC     = 7;
	
	private static final int ACTION_RESIZE_COLUMN = 8;
	private static final int ACTION_MAX = 9;
	
	private Composite    parent ;
	private ToolItem[]   toolItem;
	private LabelMessage lblMessage;
	
	private IMetricManager metricManager;
	private RootScope      root;
	private ScopeTreeTable table ;
	private ZoomAction     zoomAction;

	private ProfilePart  profilePart;
	private IEventBroker eventBroker;
	private IUndoableActionManager actionManager;
	private IScopeTreeData treeData;
	
	/*****
	 * Constructor to create a view
	 * @param parent the folder to be attached
	 * @param style SWT style 
	 * @param title the title for this view
	 */
	protected AbstractTableView(CTabFolder parent, int style, String title) {
		super(parent, style);
		setText(title);
		actionManager = new UndoableActionManager();
	}

    
    /***
     * display an error message for a couple of seconds
     * @param message
     */
	@Override
    public void showErrorMessage(String message) {
    	lblMessage.showErrorMessage(message);
    }
    
    /***
     * Display a normal message
     * @param message  
     */
	@Override
    public void showInfo(String message) {
    	lblMessage.showInfo(message);
    }
    
	/***
	 * Display a warning message
	 * @param message
	 */
	@Override
    public void showWarning(String message) {
    	lblMessage.showWarning(message);
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

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		GridData compositeGD = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		compositeGD.widthHint = 506;
		composite.setLayoutData(compositeGD);
				
		ToolBar toolBar = new ToolBar(composite, SWT.FLAT | SWT.RIGHT);

		// -------------------------------------------
		// add the beginning of toolbar
		// -------------------------------------------
		beginToolbar(null, toolBar);
		
		// -------------------------------------------
		// default tool bar
		// -------------------------------------------
		toolItem = new ToolItem[ACTION_MAX];
		
		toolItem[ACTION_ZOOM_IN]  = createToolItem(toolBar, IconManager.Image_ZoomIn,  "Zoom-in the selected node");
		toolItem[ACTION_ZOOM_OUT] = createToolItem(toolBar, IconManager.Image_ZoomOut, "Zoom-out from the current tree scope");
		toolItem[ACTION_HOTPATH]  = createToolItem(toolBar, IconManager.Image_FlameIcon, "Expand the hot path below the selected node");
		
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		toolItem[ACTION_ADD_METRIC]  = createToolItem(toolBar, IconManager.Image_FnMetric, "Create a new user-derived metric");
		toolItem[ACTION_EXPORT_DATA] = createToolItem(toolBar, IconManager.Image_SaveCSV,  "Export the current table into a CSV file");
		
		new ToolItem(toolBar, SWT.SEPARATOR);

		toolItem[ACTION_COLUMN_HIDE] = createToolItem(toolBar, IconManager.Image_CheckColumns,  "Show/hide columns");
		var mode = TableFitting.getFittingMode();
		mode = TableFitting.getNext(mode);
		var imageMode = getFittingModeImageLabel(mode);
		toolItem[ACTION_RESIZE_COLUMN] = createToolItem(toolBar, imageMode,  TOOLTIP_AUTOFIT + TableFitting.toString(mode));
		
		new ToolItem(toolBar, SWT.SEPARATOR);

		toolItem[ACTION_FONT_INC] = createToolItem(toolBar, IconManager.Image_FontBigger,  "Increase the font size");
		toolItem[ACTION_FONT_DEC] = createToolItem(toolBar, IconManager.Image_FontSmaller, "Decrease the font size");
		
		// -------------------------------------------
		// add the end of toolbar
		// -------------------------------------------
		endToolbar(null, toolBar);

		// -------------------------------------------
		// message label
		// -------------------------------------------

		lblMessage = new LabelMessage(composite, SWT.NONE);
	}
	
	private String getFittingModeImageLabel(ColumnFittingMode mode) {
		if (mode == ColumnFittingMode.FIT_DATA)
			return IconManager.Image_TableFitData;
		
		return IconManager.Image_TableFitBoth;
	}
	
	private void refreshAutoFitIcon() {
		var mode = TableFitting.getFittingMode();
		mode = TableFitting.getNext(mode);
		final var imageMode = getFittingModeImageLabel(mode);
		final var image = IconManager.getInstance().getImage(imageMode);
		toolItem[ACTION_RESIZE_COLUMN].setImage(image);
		toolItem[ACTION_RESIZE_COLUMN].setToolTipText(TOOLTIP_AUTOFIT + TableFitting.toString(mode));
	}

	@Override
	public void setInput(Object input) {
		if (!(input instanceof IMetricManager))
			return;
		
		createTable((IMetricManager) input);
	}

	
	protected void createTable(IMetricManager input) {
		
		// -------------------------------------------
		// table creation
		// -------------------------------------------
		
		metricManager = input;
		
		// -------------------------------------------
		// Create the main table
		// -------------------------------------------
		
		root = getRoot();
		treeData = getTreeData(root, metricManager);
		
		table = new ScopeTreeTable(parent, SWT.NONE, treeData);
		table.addSelectionListener(scope -> updateButtonStatus());
		
		table.addActionListener(scope -> {
			profilePart.addEditor(scope);
		});
		//
		// fix issue #188: force the table to have a content so natTable can properly resize the columns
		// this doesn't really slow the UI so it's okay to do this to all tables. 
		// 
		table.setRoot(root);
		
		updateButtonStatus();

		addButtonListener(root, metricManager);

		eventBroker.subscribe(ViewerDataEvent.TOPIC_HPC_METRIC_UPDATE,  this);
		eventBroker.subscribe(ViewerDataEvent.TOPIC_HIDE_SHOW_COLUMN,   this);
		
		eventBroker.subscribe(ViewerDataEvent.TOPIC_FILTER_PRE_PROCESSING, this);
		eventBroker.subscribe(ViewerDataEvent.TOPIC_FILTER_POST_PROCESSING, this);
		
		eventBroker.subscribe(AUTOFIT_EVENT, this);
	}
	
	
	@Override
	public Object getInput() {
		return getMetricManager();
	}

	/****
	 * Retrieve the list of metrics to be filtered.
	 * The list contains all metrics, including "unmodifiable" metrics just for the
	 * sake of convenience to show users that these metrics exist.
	 * Most "unmodifiable" metrics are because they are empty. 
	 * 
	 * @return list of filter data of metrics to be filtered
	 */
	@Override
	public List<FilterDataItem<BaseMetric>> getFilterDataItems() {
		final List<BaseMetric> listAllMetrics = getMetricManager().getVisibleMetrics();	
		
		// List of indexes of the hidden columns based on the info from the table.
		// the index starts with tree column. Hence index 0 is the tree column,
		//  and after that are the metric columns.
		final int []hiddenColumnIndexes = table.getHiddenColumnIndexes();

		final List<FilterDataItem<BaseMetric>> list = new ArrayList<>(listAllMetrics.size());
		
		// fill up the visible metrics first
		for(int i=0; i<treeData.getMetricCount(); i++) {
			boolean enabled = true;
			boolean checked = isShown(i, hiddenColumnIndexes);
			BaseMetric metric = treeData.getMetric(i);
			FilterDataItem<BaseMetric> item = new MetricFilterDataItem(metric, checked, enabled);
			list.add(item);
		}
		final List<Integer> indexes = metricManager.getNonEmptyMetricIDs(root);
		
		// fill up the "invisible" metrics		
		for(int i=0; i<listAllMetrics.size(); i++) {
			final BaseMetric metric = listAllMetrics.get(i);
			if (root.getMetricValue(metric) == MetricValue.NONE 
					&& !indexes.contains(metric.getIndex())) {
				final boolean checked = false;
				final boolean enabled = false;
				
				FilterDataItem<BaseMetric> item = new MetricFilterDataItem(metric, checked, enabled);
				list.add(item);
			}			
		}
		return list;
	}
	
	private boolean initialized = false;
	
	@Override
	public void activate() {
		if (!initialized) {
			// Warning: this process takes time
			BusyIndicator.showWhile(getDisplay(), ()-> {
				root = buildTree(false);
				table.setRoot(root);
				
				// flip the flag
				initialized = true;
			});
		} else {
			table.visualRefresh();
		}
		table.setFocus();
	}

	
	private boolean isShown(int metricIndex, final int []hiddenColumnIndexes)  {
		
		// FIXME: ugly code. Try to find if this metric is hidden or not by
		//        checking if the metric order is the hidden column indexes
		for (int j=0; j<hiddenColumnIndexes.length; j++) {
			// Recall that hiddenColumnIndexes starts with 1. The 0 value is for tree column
			if (hiddenColumnIndexes[j] == metricIndex+1) {
				return false;
			}
		}
		return true;
	}

	@Override
	public IMetricManager getMetricManager() {
		return metricManager;
	}


	/**
	 * Temporary array to store the current expanded nodes.
	 * Only computed before filtering, and use it after filtering.
	 * After that, there is no use.
	 */
	private int []expandedNodes;
	
	@Override
	public void handleEvent(Event event) {
		if (table == null) {
			eventBroker.unsubscribe(this);
			return;
		}
		Object obj = event.getProperty(IEventBroker.DATA);
		if (obj == null || metricManager == null)
			return;
		
		if (!(obj instanceof ViewerDataEvent)) {
			if (event.getTopic().equals(AUTOFIT_EVENT))
				refreshAutoFitIcon();
			
			return;
		}
			
		ViewerDataEvent eventInfo = (ViewerDataEvent) obj;
		if (metricManager != eventInfo.metricManager) 
			return;

		String topic = event.getTopic();
		if (topic.equals(ViewerDataEvent.TOPIC_HIDE_SHOW_COLUMN)) {
			
			MetricDataEvent dataEvent = (MetricDataEvent) eventInfo.data;
			
			// if the hide/show event is only for the current active view,
			// we need to check if this one if the active or not
			// if not, just leave it
			
			if (dataEvent.isApplyToAll() || profilePart.getActiveView() == this) {
				hideORShowColumns(dataEvent);
			}
			
		} else if (topic.equals(ViewerDataEvent.TOPIC_HPC_METRIC_UPDATE)) {
			// metric has changed. 
			// We don't know if the change will incur structural changes or just visual.
			// it's better to refresh completely the table just in case. 
			table.visualRefresh();

		} else if (topic.equals(ViewerDataEvent.TOPIC_FILTER_PRE_PROCESSING)) {
			expandedNodes = table.getPathOfSelectedNode();
			
		} else if (topic.equals(ViewerDataEvent.TOPIC_FILTER_POST_PROCESSING)) {
			RootScope newRoot = this.buildTree(true);
			actionManager.clear();
			
			table.reset(newRoot);
			
			// put back the expanded tree and the selected node
			if (expandedNodes != null) {
				table.expandAndSelectNode(expandedNodes);
				expandedNodes = null;
			}
			
			updateButtonStatus();
		}
	}
	
	

	@Override
	public void dispose() {
		if (eventBroker != null)
			eventBroker.unsubscribe(this);
		
		if (table != null)
			table.dispose();
		
		if (actionManager != null)
			actionManager.clear();
		
		actionManager = null;
		metricManager = null;
		profilePart = null;
		root = null;
		table = null;
		treeData = null;
		zoomAction = null;
		
		super.dispose();
	}

	
	@Override
	public ViewType getViewType() {
		// quick fix for merged database:
		// - for normal databases we'll have more than 2 views
		// - for merged database, we'll have only 1 view
		//
		if (metricManager instanceof Experiment) {
			Experiment exp = (Experiment) metricManager;
			if (exp.isMergedDatabase())
				return BaseConstants.ViewType.INDIVIDUAL;
		}
		return BaseConstants.ViewType.COLLECTIVE;
	}
	
	
	private void hideORShowColumns(MetricDataEvent dataEvent) {
		Object objData = dataEvent.getData();
		
		// two possibilities of data types for this event:
		// - List: it contains the list of columns to be shown/hidden
		// - MetricFilterDataItem: a column to be shown/hidden 
		var nonEmptyMetricId = metricManager.getNonEmptyMetricIDs(root);
		final List<BaseMetric> metrics = getMetricManager().getVisibleMetrics();
		final List<BaseMetric> visibleMetrics = new ArrayList<>();
		for(var id: nonEmptyMetricId) {
			var metric = metrics.stream().filter(m->m.getIndex() == id).findAny();
			if (metric.isPresent()) {
				visibleMetrics.add(metric.get());
			}
		}
		
		if (objData instanceof List<?>) {
			@SuppressWarnings("unchecked")
			final List<MetricFilterDataItem> list = (List<MetricFilterDataItem>) objData;
			
			// create the list of column status: true if shown, false if hidden
			for(MetricFilterDataItem item: list) {
				if (!item.enabled)
					continue;
				
				int i = getColumnIndexByMetric(item, visibleMetrics);
				hideOrShowColumn(i, item.checked);
			}
			
		} else if (objData instanceof MetricFilterDataItem) {
			MetricFilterDataItem item = (MetricFilterDataItem) objData; 
			
			int index = getColumnIndexByMetric(item, visibleMetrics);
			hideOrShowColumn(index, item.checked);
		}
		// Need to resize the column in case some columns are hidden and need to resize
		// the tree column
		table.pack();
		
		// hide or show columns cause visual changes.
		// however, we are forced to refresh completely the whole layer of the table.
		// Fix issue #214: calling visualRefresh won't reset the column position
		// On the contrary, call refresh() method will reset the position.
		table.visualRefresh();
		
		// have to freeze again the tree column. 
		// Sometimes after the hide/show the frozen attribute disappears and the column is not frozen anymore
		// I don't know why.
		table.freezeTreeColumn();
	}
	
	private synchronized void hideOrShowColumn(int columnIndex, boolean shown) {
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
			if (metric.equalIndex(m)) {
				return index;
			}
			if (!isMetricToSkip(root, m))
				index++;
		}
		return -1;
	}
	

	/***
	 * Check if a certain metric needs to be hidden all the time or not.
	 * To save the memory, we usually hide empty columns. 
	 * However, the children can display them if necessary.
	 * 
	 * @param scope
	 * 			the current scope. Usually the root of the table
	 * @param metric
	 * 			the metric which to be verified whether to be skipped or not.
	 * @return
	 * 			boolean true if it has to be skipped. False otherwise. 
	 */
	protected boolean isMetricToSkip(Scope scope, BaseMetric metric) {
		MetricValue mv = scope.getMetricValue(metric);

		// empty metric is not visible (usually).
		// the column index should be based on non-empty metrics
		return mv == MetricValue.NONE;
	}

	
	private void addButtonListener(RootScope root, IMetricManager metricManager) {
		
		zoomAction = new ZoomAction(actionManager, table);	

		toolItem[ACTION_ZOOM_IN].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				Scope selection = table.getSelection();
				if (selection != null)
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
				updateButtonStatus();
			}
		});

		toolItem[ACTION_ADD_METRIC].addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				UserDerivedMetric derivedMetricAction = new UserDerivedMetric(root, metricManager, eventBroker);
				derivedMetricAction.addNewMeric();
				updateButtonStatus();
			}			
		});
		
		toolItem[ACTION_COLUMN_HIDE].addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				MetricFilterInput input = new MetricFilterInput(AbstractTableView.this);
				profilePart.addEditor(input);
				updateButtonStatus();
			}
		});
		
		toolItem[ACTION_RESIZE_COLUMN].addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					TableFitting.fitTable(table);
				} catch (IOException err) {
					MessageDialog.openError(table.getTable().getShell(), "Error saving preferences", err.getMessage());
					return;
				}
				// the autofit button changes the icon every time user click
				// this makes complicated when the user click autofit in one view
				// but then switch to another view. This view needs to refresh
				// the autofit button based on the current mode.
				//
				// next time, don't make things complicated

				eventBroker.post(AUTOFIT_EVENT, TableFitting.getFittingMode());
			}
		});
		
		toolItem[ACTION_EXPORT_DATA].addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				table.export();
			}
		});
		
		toolItem[ACTION_FONT_INC].addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				FontManager.changeFontHeight(1);
			}
		});
		
		toolItem[ACTION_FONT_DEC].addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				FontManager.changeFontHeight(-1);
			}
		});
	}
	
	protected IUndoableActionManager getActionManager() {
		return actionManager;
	}
	
	protected ProfilePart getProfilePart() {	
		return profilePart;
	}
	
	
	protected IScopeTreeAction getTable() {
		return table;
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
		toolItem[ACTION_EXPORT_DATA].setEnabled(true);
		toolItem[ACTION_FONT_INC].setEnabled(true);
		toolItem[ACTION_FONT_DEC].setEnabled(true);
		toolItem[ACTION_RESIZE_COLUMN].setEnabled(true);
		
		Scope selectedScope = table.getSelection();
		boolean canZoomIn = zoomAction != null && zoomAction.canZoomIn(selectedScope); 
		toolItem[ACTION_ZOOM_IN].setEnabled(canZoomIn);
		
		boolean canZoomOut = zoomAction != null && zoomAction.canZoomOut();
		toolItem[ACTION_ZOOM_OUT].setEnabled(canZoomOut);
		
		boolean canHotPath = selectedScope != null && selectedScope.hasChildren();
		toolItem[ACTION_HOTPATH].setEnabled(canHotPath);
	}
	
	public abstract RootScopeType getRootType();
	
	protected abstract RootScope buildTree(boolean reset);
    protected abstract void beginToolbar(CoolBar coolbar, ToolBar toolbar);
    protected abstract void endToolbar  (CoolBar coolbar, ToolBar toolbar);
    protected abstract void updateStatus();
    protected abstract IScopeTreeData getTreeData(RootScope root, IMetricManager metricManager);
}
