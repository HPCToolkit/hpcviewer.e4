package edu.rice.cs.hpcviewer.ui.tabItems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.IMetricManager;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.TreeNode;
import edu.rice.cs.hpc.filter.service.FilterStateProvider;
import edu.rice.cs.hpcbase.BaseConstants;
import edu.rice.cs.hpcbase.ViewerDataEvent;
import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.base.IViewBuilder;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;
import edu.rice.cs.hpcviewer.ui.util.SortColumn;


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

	private IViewBuilder contentViewer;
	
	/** Each view needs to store the experiment database.
	 * In case it needs to populate the table, we know which database 
	 * to be loaded. */
	private BaseExperiment  experiment;
	
	/** This variable is a flag whether a table is already populated or not.
	 * If the root is null, it isn't populated
	 */
	private RootScope       root;

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
		eventBroker.subscribe(BaseConstants.TOPIC_HPC_REMOVE_DATABASE, this);
		eventBroker.subscribe(ViewerDataEvent.TOPIC_HIDE_SHOW_COLUMN,    this);
		eventBroker.subscribe(ViewerDataEvent.TOPIC_HPC_ADD_NEW_METRIC,  this);
		eventBroker.subscribe(ViewerDataEvent.TOPIC_HPC_METRIC_UPDATE,   this);
		
		// subscribe to filter events
		eventBroker.subscribe(FilterStateProvider.FILTER_REFRESH_PROVIDER, this);
	}
	
	@Override
	public void setInput(Object input) {
		
		if (!(input instanceof BaseExperiment))
			return;
					
		// important: needs to store the experiment database for further usage
		// when the view is becoming visible
		this.experiment = (BaseExperiment) input;
				
		// TODO: this process takes time
		root = createRoot(experiment);
		contentViewer.setData(root);
	}

	private void filter() {
		FilterStateProvider.filterExperiment((Experiment) experiment);
		
		final ScopeTreeViewer treeViewer = contentViewer.getTreeViewer();
		final Tree tree = treeViewer.getTree();
		
		// store the current selected node and sorted column 
		Scope selectedNode    = treeViewer.getSelectedNode();
		int sortDirection 	  = tree.getSortDirection();
		TreeColumn sortColumn = tree.getSortColumn();
		int sortColumnIndex   = getSortColumnIndex(sortColumn, tree);
		
		List<Scope> path = new ArrayList<Scope>();
		if (selectedNode != null) {
			Scope parent = selectedNode.getParentScope();
			while(parent != null && !(parent instanceof RootScope)) {
				path.add(parent);
				parent = parent.getParentScope();
			}
			Collections.reverse(path);
		}

		PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		boolean debug = pref.getBoolean(PreferenceConstants.ID_DEBUG_MODE);

		long t0 = System.currentTimeMillis();

		// TODO: this process takes time
		root = createRoot(experiment);
		contentViewer.setData(root, sortColumnIndex, SortColumn.getSortDirection(sortDirection));

		long t1 = System.currentTimeMillis();

		if (debug) {
			System.out.println(getClass().getSimpleName() + ". time to filter: " + (t1-t0) + " ms");
		}
		
		TreeItem item = expand(treeViewer, tree.getTopItem(), path);
		tree.select(item);
		
		long t2 = System.currentTimeMillis();
		if (debug) {
			System.out.println(getClass().getSimpleName() + ". time to expand: " + (t2-t1) + " ms");
		}
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
	private TreeItem expand(TreeViewer treeViewer, TreeItem item, List<Scope> path) {
		
		if (item == null)
			return null;

		// try to reveal the parent first.
		// sometimes in virtual table, the parent item is not materialized yet
		Scope oi = (Scope) item.getData();
		if (oi != null)
			treeViewer.reveal(oi);
		
		// materialize the children of the item
		// this is important for virtual tree (or table)
		// without materializing the item, the content is empty, and the
		// node is named "{*virtual*}"
		List<TreeNode> listChildren = oi.getListChildren();
		if (listChildren == null) 
			return item;
		
		listChildren.stream().forEach((c) -> {
			treeViewer.reveal(c);
		});
		
		TreeItem []items = item.getItems();
		if (items == null || path.size()==0)
			return item;
		
		final Scope node = path.remove(0);
		final int cctPath = node.getCCTIndex();

		// Tree traversal to reveal the path:
		// - check if the child item has the same cct-id as the node in the path.
		//   if they are the same, then recursively expand the child
		// - if no child has the same cct-id as the one in the path, do nothing
		for(TreeItem child: items) {
			treeViewer.reveal(child);
			Object o = child.getData();
			if (o != null && (o instanceof Scope)) {
				int cctItem = ((Scope)o).getCCTIndex();
				if (cctItem == cctPath) {
					return expand(treeViewer, child, path);
				}
			}
		} 
		return item;
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
		if (treeViewer.getTree().isDisposed())
			return;

		Object obj = event.getProperty(IEventBroker.DATA);
		if (obj == null || experiment == null || root == null)
			return;
		
		if (!(obj instanceof ViewerDataEvent)) {
			if (event.getTopic().equals(FilterStateProvider.FILTER_REFRESH_PROVIDER)) {
				filter();
				// notify trace viewer to update the call path
				ViewerDataEvent data = new ViewerDataEvent((Experiment) experiment, obj);
				eventBroker.post(ViewerDataEvent.TOPIC_HPC_DATABASE_REFRESH, data);
			}
			return;
		}
		
		ViewerDataEvent eventInfo = (ViewerDataEvent) obj;
		if (experiment != eventInfo.experiment) 
			return;
		
		String topic = event.getTopic();
		if (topic.equals(ViewerDataEvent.TOPIC_HIDE_SHOW_COLUMN)) {
			IMetricManager mgr = eventInfo.experiment;
			treeViewer.setColumnsStatus(mgr, (boolean[]) eventInfo.data);
			
		} else if (topic.equals(ViewerDataEvent.TOPIC_HPC_ADD_NEW_METRIC)) {
			treeViewer.addUserMetricColumn((BaseMetric) eventInfo.data);

		} else if (topic.equals(BaseConstants.TOPIC_HPC_REMOVE_DATABASE)) {
			// mark that this part will be destroyed
			experiment.dispose();
			experiment = null;

		} else if (topic.equals(ViewerDataEvent.TOPIC_HPC_METRIC_UPDATE)) {
			treeViewer.refreshColumnTitle();
		}
	}
	
	public boolean focus () {
		ScopeTreeViewer viewer = contentViewer.getTreeViewer();
		return viewer.getTree().forceFocus();
	}

	private int getSortColumnIndex(TreeColumn sortColumn, Tree tree) {
		int sortColumnIndex = 0;
		for(TreeColumn col : tree.getColumns()) {
			if (col == sortColumn) 
				break;
			sortColumnIndex++;
		}
		return sortColumnIndex;
	}
	
	protected abstract RootScope 	  createRoot(BaseExperiment experiment);
	protected abstract IViewBuilder   setContentViewer(Composite parent, EMenuService menuService);
	protected abstract RootScopeType  getRootType();

}
