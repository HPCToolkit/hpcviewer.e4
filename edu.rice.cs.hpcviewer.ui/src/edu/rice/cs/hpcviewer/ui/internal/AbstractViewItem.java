package edu.rice.cs.hpcviewer.ui.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcbase.BaseConstants;
import edu.rice.cs.hpcbase.ViewerDataEvent;
import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.base.IViewBuilder;
import edu.rice.cs.hpcviewer.ui.metric.MetricView.MetricDataEvent;


/*******************************************************************************************
 * 
 * Abstract class to manage basic tab item such as:
 * <ul>
 * <li>Standard events</li>
 * <li>Table viewer</li>
 * <li>Standard actions</li>
 * <li>and so on</li>
 * </ul>
 *
 *******************************************************************************************/
public abstract class AbstractViewItem extends AbstractBaseViewItem implements EventHandler
{

	protected EPartService  partService;
	protected EModelService modelService;
	protected MApplication  app;
	protected IEventBroker  eventBroker;
	protected EMenuService  menuService;
	
	protected DatabaseCollection databaseAddOn;
	protected ProfilePart   profilePart;

	protected IViewBuilder contentViewer;
	
	/** Each view needs to store the experiment database.
	 * In case it needs to populate the table, we know which database 
	 * to be loaded. */
	protected BaseExperiment  experiment;
	
	/** This variable is a flag whether a table is already populated or not.
	 * If the root is null, it isn't populated
	 */
	protected RootScope       root;
	
	private List<FilterDataItem> listHideShowMetrics;

	public AbstractViewItem(CTabFolder parent, int style) {
		super(parent, style);
	}

	
	@Override
	public void setService(EPartService partService, 
			IEventBroker broker,
			DatabaseCollection database,
			ProfilePart   profilePart,
			EMenuService  menuService) {
		
		this.partService = partService;
		this.eventBroker = broker;
		this.databaseAddOn = database;
		this.profilePart = profilePart;
		this.menuService = menuService;
	}
	
	
	@Override
	public void createContent(Composite parent) {
		contentViewer = setContentViewer(parent, menuService);
    	contentViewer.createContent(profilePart, parent, menuService);
    	
		// subscribe to user action events
		eventBroker.subscribe(BaseConstants.TOPIC_HPC_REMOVE_DATABASE,  this);
		eventBroker.subscribe(ViewerDataEvent.TOPIC_HIDE_SHOW_COLUMN,   this);
		eventBroker.subscribe(ViewerDataEvent.TOPIC_HPC_ADD_NEW_METRIC, this);
		eventBroker.subscribe(ViewerDataEvent.TOPIC_HPC_METRIC_UPDATE,  this);
		eventBroker.subscribe(ViewerDataEvent.TOPIC_HPC_DATABASE_REFRESH, this);
	}
	
	@Override
	public void setInput(Object input) {
		
		if (!(input instanceof BaseExperiment))
			return;
					
		// important: needs to store the experiment database for further usage
		// when the view is becoming visible
		this.experiment = (BaseExperiment) input;
	}

	
	@Override
	public void activate() {
		if (root == null) {			
			// TODO: this process takes time
			BusyIndicator.showWhile(getDisplay(), ()-> {
				root = createRoot(experiment);
				contentViewer.setData(root);
				
				// hide or show columns if needed
				if (listHideShowMetrics != null && listHideShowMetrics.size()>0) {
					listHideShowMetrics.stream().forEach(data -> {
						AbstractBaseViewItem.updateColumnHideOrShowStatus(contentViewer.getTreeViewer(), data);
					});
				}
			});
		}
	}
	
	
	/****
	 * Retrieve the current input of this view
	 * @return
	 */
	@Override
	public Object getInput() {
		return experiment;
	}
	
	
	@Override
	public void handleEvent(Event event) {
		ScopeTreeViewer treeViewer = contentViewer.getTreeViewer();
		if (treeViewer.getTree().isDisposed()) {
			eventBroker.unsubscribe(this);
			return;
		}

		Object obj = event.getProperty(IEventBroker.DATA);
		if (obj == null || experiment == null)
			return;
		
		if (!(obj instanceof ViewerDataEvent)) 
			return;
		
		ViewerDataEvent eventInfo = (ViewerDataEvent) obj;
		if (experiment != eventInfo.experiment) 
			return;
		
		// special case: the table is not populated yet, probably is not activated.
		// we need to store the current hide/show metric in the list and use it
		// when the table is populated
		
		if (root == null) {
			if (event.getTopic().equals(ViewerDataEvent.TOPIC_HIDE_SHOW_COLUMN)) {
				MetricDataEvent dataEvent = (MetricDataEvent) eventInfo.data;
				if (dataEvent.isApplyToAll()) {
					listHideShowMetrics = dataEvent.getList();
				}
			}
			return;
		}

		String topic = event.getTopic();
		if (topic.equals(ViewerDataEvent.TOPIC_HIDE_SHOW_COLUMN)) {
			
			MetricDataEvent dataEvent = (MetricDataEvent) eventInfo.data;
			
			// if the hide/show event is only for the current active view,
			// we need to check if this one if the active or not
			// if not, just leave it
			
			if (!dataEvent.isApplyToAll()) {
				AbstractBaseViewItem activeView = profilePart.getActiveView();
				if (this != activeView)
					return;
			}
			AbstractBaseViewItem.updateColumnHideOrShowStatus(getScopeTreeViewer(), dataEvent.getData());
			
		} else if (topic.equals(ViewerDataEvent.TOPIC_HPC_ADD_NEW_METRIC)) {
			treeViewer.addUserMetricColumn((BaseMetric) eventInfo.data);

		} else if (topic.equals(BaseConstants.TOPIC_HPC_REMOVE_DATABASE)) {
			// mark that this part will be destroyed
			experiment.dispose();
			experiment = null;

		} else if (topic.equals(ViewerDataEvent.TOPIC_HPC_METRIC_UPDATE)) {
			treeViewer.refreshColumnTitle();
			
		} else if (topic.equals(ViewerDataEvent.TOPIC_HPC_DATABASE_REFRESH)) {
			BusyIndicator.showWhile(getDisplay(), ()-> {
				refreshTree();
			});
		}
	}
	
	
	public ScopeTreeViewer getScopeTreeViewer() {
		return contentViewer.getTreeViewer();
	}
	
	/****
	 * Reset the content of the table.
	 * Call this method if the tree has changed like after filtering
	 * 
	 */
	protected void refreshTree() {
		
		final ScopeTreeViewer treeViewer = contentViewer.getTreeViewer();
		final Tree tree = treeViewer.getTree();
		
		// 1. store the path to the current selected node and the sorted column 
		Scope selectedNode    = treeViewer.getSelectedNode();
		
		// 2. reverse the path from bottom-up to the top-down
		List<Scope> path = new ArrayList<Scope>();
		if (selectedNode != null) {
			Scope parent = selectedNode.getParentScope();
			while(parent != null && !(parent instanceof RootScope)) {
				path.add(parent);
				parent = parent.getParentScope();
			}
			Collections.reverse(path);
		}

		// 3. store the column width
		//    should we store all columns' width or just the tree? 
		//    I argue just the tree is enough. Resizing columns width for GPU database
		//    can be time consuming
		long t0 = System.currentTimeMillis();
		int scopeWidth = tree.getColumn(0).getWidth();
		int sortDirection = tree.getSortDirection();
		
		// 4. reset the data
		root = createRoot(experiment);
		Scope rootTable = root.createRoot();
		treeViewer.setInput(rootTable);
		
		long t1 = System.currentTimeMillis();
		LoggerFactory.getLogger(getClass()).debug("Time to reset: " + (t1-t0) + " ms");

		// 5. restore stuffs:
		//    - the path to the selected node, 
		//    - the sorted column
		//    - the column's width
		//    On Linux GTK, this requires asynchronous UI thread because the widget may not
		//    be ready to materialize tree items
		
		Display.getDefault().asyncExec(()-> {
			
			try {
				tree.setRedraw(false);
				
				// on Linux/GTK we need to preserve the sort direction of the column
				// otherwise, it will disappear. This problem doesn't occur on other platforms
				tree.setSortDirection(sortDirection);
				
				TreePath treePath = new TreePath(new Object[] {root});
				TreePath newPath = expand(treeViewer, treePath, path);
				if (newPath != null) {
					//treeViewer.reveal(newPath);
					treeViewer.setSelection(new StructuredSelection(newPath), true);
				}
				tree.getColumn(0).setWidth(scopeWidth);
			} finally {
				tree.setRedraw(true);
			}

			long t2 = System.currentTimeMillis();
			LoggerFactory.getLogger(getClass()).debug("Time to expand: " + (t2-t1) + " ms");
		});
	}

	
	/****
	 * Expand a tree to a specified path.
	 * If the path doesn't exist in the tree, try to expand
	 * as many as possible according the most recent recognized path.
	 * 
	 * @see issue #79 (preserving tree expansion)
	 * 
	 * @param treeViewer the current trace viewer
	 * @param item the parent item
	 * @param path {@code List<Scope>} the list of path
	 */
	protected TreePath expand(TreeViewer treeViewer, TreePath treePath, List<Scope> path) {
		
		if (treePath == null || path == null  || path.size()==0)
			return treePath;

		// try to reveal the parent first.
		// sometimes in virtual table, the parent item is not materialized yet
		Scope lastElement = (Scope) treePath.getLastSegment();
		
		if (lastElement == null)
			return treePath;
		
		final Scope node = path.remove(0);
		final int cctPath = node.getCCTIndex();

		// Tree traversal to reveal the path:
		// - check if the child item has the same cct-id as the node in the path.
		//   if they are the same, then recursively expand the child
		// - if no child has the same cct-id as the one in the path, do nothing
		Object []items = lastElement.getChildren();
		if (items == null)
			return treePath;
		
		for (Object item: items) {
			if (item != null) {
				Scope child = (Scope) item;
				int cctItem = child.getCCTIndex();
				if (cctItem == cctPath) {
					treePath = treePath.createChildPath(child);
					treeViewer.setExpandedState(child, true);
					return expand(treeViewer, treePath, path);
				}
			}
		}
		return treePath;
	}
	
	public boolean focus () {
		ScopeTreeViewer viewer = contentViewer.getTreeViewer();
		return viewer.getTree().forceFocus();
	}

	protected abstract RootScope 	  createRoot(BaseExperiment experiment);
	protected abstract IViewBuilder   setContentViewer(Composite parent, EMenuService menuService);
	protected abstract RootScopeType  getRootType();

}
