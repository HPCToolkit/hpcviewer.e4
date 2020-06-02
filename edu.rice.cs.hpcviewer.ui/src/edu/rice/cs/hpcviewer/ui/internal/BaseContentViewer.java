package edu.rice.cs.hpcviewer.ui.internal;

import javax.annotation.PreDestroy;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpcviewer.ui.parts.IContentViewer;
import edu.rice.cs.hpcviewer.ui.resources.IconManager;


/****
 * 
 * Base class to manage the content of the view part
 * 
 * A part has to call {@link IContentViewer.createContent} to create the content of the view
 * (like toolbar and table tree).
 * 
 * For further customization, the caller (or part) has to subclass this class and implement
 * {@link beginToolbar} and {@link endToolbar}
 */
public abstract class BaseContentViewer implements IContentViewer, ISelectionChangedListener
{
	final int TREE_COLUMN_WIDTH  = 250;

	final private EPartService  partService;
	final private EModelService modelService;
	final private MApplication  app;
	
	private ScopeTreeViewer treeViewer = null;
	
	private ToolItem     toolItem[];
	private LabelMessage lblMessage;
	
	private Listener mouseDownListener = null;
	private StyledScopeLabelProvider labelProvider;
	
	private ScopeZoom zoomAction = null;
	
	public BaseContentViewer(
			EPartService  partService, 
			EModelService modelService,
			MApplication  app) {
		
		this.partService  = partService;
		this.modelService = modelService;
		
		this.app = app;
	}
	
	@Override
	public void createContent(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(parent, SWT.NONE);
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
		toolItem = new ToolItem[8];
		
		toolItem[ActionType.ZOOM_IN.getValue()]  = createToolItem(toolBar, IconManager.Image_ZoomIn,    "Zoom-in the selected node");
		toolItem[ActionType.ZOOM_OUT.getValue()] = createToolItem(toolBar, IconManager.Image_ZoomOut,   "Zoom-out from the current root");
		toolItem[ActionType.HOTPATH.getValue()]  = createToolItem(toolBar, IconManager.Image_FlameIcon, "Expand the hot path below the selected node");

		toolItem[ActionType.DERIVED_METRIC.getValue()] = createToolItem(toolBar, IconManager.Image_FnMetric,     "Add a new derived metric");
		toolItem[ActionType.COLUMN_HIDE.getValue()]    = createToolItem(toolBar, IconManager.Image_CheckColumns, "Hide/show columns");
		toolItem[ActionType.EXPORT_DATA.getValue()]    = createToolItem(toolBar, IconManager.Image_SaveCSV,  	"Export the current view into a comma separated value file");

		toolItem[ActionType.FONT_BIGGER.getValue()]  = createToolItem(toolBar, IconManager.Image_FontBigger,  "Increase font size");
		toolItem[ActionType.FONT_SMALLER.getValue()] = createToolItem(toolBar, IconManager.Image_FontSmaller, "Decrease font size");
		
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

		
		// -------------------------------------------
		// table creation
		// -------------------------------------------
		
		treeViewer = new ScopeTreeViewer(parent, SWT.BORDER | SWT.MULTI);
		Tree tree = treeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

		treeViewer.setContentProvider( getContentProvider(treeViewer));
		createScopeColumn(treeViewer);
		
		mouseDownListener = new ScopeMouseListener(treeViewer, partService, modelService, app);
		treeViewer.getTree().addListener(SWT.MouseDown, mouseDownListener);
		treeViewer.addSelectionChangedListener(this);

		// initialize tool item handler at the end
		// because we need access to tree viewer :-( 
		setToolItemHandlers();
	}

	@PreDestroy
	public void dispose() {
		treeViewer.removeSelectionChangedListener(this);
		((ScopeMouseListener) mouseDownListener).dispose();
		
	}
	
	@Override
	public void setData(RootScope root) {
		
		removeColumns();
		
		createScopeColumn(getViewer());
		
		Experiment experiment = (Experiment) root.getExperiment();
		
		// add metric columns only if the metric is not empty
		boolean bSorted = true;
		BaseMetric metrics[] = experiment.getMetrics();
		
		for(BaseMetric metric : metrics) {
			if (root.getMetricValue(metric) == MetricValue.NONE)
				continue;
			
			treeViewer.addTreeColumn(metric, bSorted);
			
			// only the first visible column is sorted
			bSorted = false;
		}
		// populate the table
		treeViewer.setInput(root);
		
		// insert the first row (header)
		treeViewer.insertParentNode(root);
		
		// resize the width of metric columns
		TreeColumn columns[] = treeViewer.getTree().getColumns();
		for(TreeColumn col:columns) {
			if (col.getData() != null) {
				col.pack();
			}
		}
	}
	
	@Override
    public void selectionChanged(SelectionChangedEvent event) {
		updateToolItemStatus();
	}
	
	
    
    /***
     * generic method to create column scope tree
     * Called by children to have uniform way to create a scope tree.
     * 
     * @param treeViewer
     * @return
     */
    protected TreeViewerColumn createScopeColumn(ScopeTreeViewer treeViewer) {

        //----------------- create the column tree
        final TreeViewerColumn colTree = new TreeViewerColumn(treeViewer,SWT.LEFT, 0);
        colTree.getColumn().setText("Scope");
        colTree.getColumn().setWidth(TREE_COLUMN_WIDTH);
        
        if (labelProvider == null) {
        	labelProvider = new StyledScopeLabelProvider();
        }
        colTree.setLabelProvider( labelProvider ); 

		return colTree;
    }

	
	private void removeColumns() {
		TreeColumn columns[] = treeViewer.getTree().getColumns();
		for (TreeColumn col : columns) {
			col.dispose();
		}
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
	
	/**
	 * Enable all tool items
	 */
	protected void updateToolItemStatus() {

		IStructuredSelection selection = treeViewer.getStructuredSelection();
		selectionChanged(selection);
		
		if (selection != null) {
			Object item = selection.getFirstElement();
			if (item instanceof Scope) {
				
				Scope node = (Scope) item;
				boolean enabled = zoomAction.canZoomIn((Scope) node);
				toolItem[ActionType.ZOOM_IN.getValue()].setEnabled(enabled);
				toolItem[ActionType.HOTPATH.getValue()].setEnabled(true);
			} else {
				toolItem[ActionType.ZOOM_IN.getValue()].setEnabled(false);
				toolItem[ActionType.HOTPATH.getValue()].setEnabled(false);
			}
			
		} else {
			// no selection: disable some
			toolItem[ActionType.ZOOM_IN.getValue()].setEnabled(false);
			toolItem[ActionType.HOTPATH.getValue()].setEnabled(false);
		}
		boolean enableZoomout = zoomAction.canZoomOut();
		toolItem[ActionType.ZOOM_OUT.getValue()].setEnabled(enableZoomout);
    }
	
	/***
	 * Retrieve the viewer
	 * @return
	 */
	protected ScopeTreeViewer getViewer() {
		return treeViewer;
	}
	

    
	/**
	 * show the hot path below the selected node in the tree
	 */
	protected void showHotCallPath() {
		// find the selected node
		ISelection sel = treeViewer.getSelection();
		if (!(sel instanceof TreeSelection)) {
			System.err.println("SVA: not a TreeSelecton instance");
			return;
		}
		TreeSelection objSel = (TreeSelection) sel;
		// get the node
		Object o = objSel.getFirstElement();
		if (!(o instanceof Scope)) {
			lblMessage.showErrorMessage("Please select a scope node.");
			return;
		}
		Scope current = (Scope) o;
		// get the item
		TreeItem item = this.treeViewer.getTree().getSelection()[0];
		// get the selected metric
		TreeColumn colSelected = this.treeViewer.getTree().getSortColumn();
		if((colSelected == null) || colSelected.getWidth() == 0) {
			// the column is hidden or there is no column sorted
			lblMessage.showErrorMessage("Please select a column to sort before using this feature.");
			return;
		}
		// get the metric data
		Object data = colSelected.getData();
		if(data instanceof BaseMetric && item != null) {
			BaseMetric metric = (BaseMetric) data;
			// find the hot call path
			int iLevel = 0;
			TreePath []path = objSel.getPaths();
			
			HotCallPath objHotPath = new HotCallPath();
			
			boolean is_found = getHotCallPath(current, metric, iLevel, path[0], objHotPath);

			// whether we find it or not, we should reveal the tree path to the last scope
			
			treeViewer.setSelection(new StructuredSelection(objHotPath.path), true);

			if(!is_found) {
				lblMessage.showErrorMessage("No hot child.");
			}
		} else {
			// It is almost impossible for the jvm to reach this part of branch.
			// but if it is the case, it should be a BUG !!
			if(data !=null )
				System.err.println("SVA BUG: data="+data.getClass()+" item= " + (item==null? 0 : item.getItemCount()));
			else
				lblMessage.showErrorMessage("Please select a metric column !");
		}
	}

    
    /**
	 * find the hot call path
	 * @param Scope scope
	 * @param BaseMetric metric
	 * @param int iLevel
	 * @param TreePath path
	 * @param HotCallPath objHotPath (caller has to allocate it)
	 */
	private boolean getHotCallPath(Scope scope, BaseMetric metric, int iLevel, TreePath path, HotCallPath objHotPath) {
		if(scope == null || metric == null )
			return false;

		AbstractContentProvider content = (AbstractContentProvider)treeViewer.getContentProvider();
		Object []children = content.getSortedChildren(scope);
		
		if (objHotPath == null) objHotPath = new HotCallPath();
		
		objHotPath.node = scope;
		objHotPath.path = path;
		
		// singly depth first search
		// bug fix: we only drill once !
		if (children != null && children.length > 0) {
			Object o = children[0];
			if(o instanceof Scope) {
				// get the child node
				Scope scopeChild = (Scope) o;
				
				// let's move deeper down the tree
				// this cause java null pointer
				try {
					treeViewer.expandToLevel(path, 1);					
				} catch (Exception e) {
					System.out.println("Cannot expand path " + path.getLastSegment() + ": " + e.getMessage());
					e.printStackTrace();
					return false;
				}

				// compare the value of the parent and the child
				// if the ratio is significant, we stop 
				MetricValue mvParent = metric.getValue(scope);
				MetricValue mvChild  = metric.getValue(scopeChild);
				
				double dParent = MetricValue.getValue(mvParent);
				double dChild  = MetricValue.getValue(mvChild);
				
				// simple comparison: if the child has "significant" difference compared to its parent
				// then we consider it as hot path node.
				if(dChild < (0.5 * dParent)) {
					objHotPath.node     = scopeChild;
					
					return true;
				} else {

					TreePath childPath = path.createChildPath(scopeChild);
					return getHotCallPath(scopeChild, metric, iLevel+ 1, childPath, objHotPath);
				}
			}
		}
		// if we reach at this statement, then there is no hot call path !
		return false;
	}

	private void setToolItemHandlers() {
		toolItem[ActionType.HOTPATH.getValue()].addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				showHotCallPath();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		zoomAction = new ScopeZoom(getViewer());

		toolItem[ActionType.ZOOM_IN.getValue()].addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {

				ScopeTreeViewer viewer = getViewer();
				ISelection selection   = viewer.getSelection();
				
				if (selection == null)
					return;
				
				IStructuredSelection structSelect = (IStructuredSelection) selection;
				Object data = structSelect.getFirstElement();
				if (data != null && data instanceof Scope) {
					zoomAction.zoomIn((Scope) data);
					
					updateToolItemStatus();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		toolItem[ActionType.ZOOM_OUT.getValue()].addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {

				zoomAction.zoomOut();
				
				updateToolItemStatus();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	}
    
    /////////////////////////////////////////////////////////
    ///
    ///  Abstract methods
    ///
    /////////////////////////////////////////////////////////
    
    protected abstract void beginToolbar(CoolBar coolbar, ToolBar toolbar);
    protected abstract void endToolbar(CoolBar coolbar, ToolBar toolbar);
    protected abstract AbstractContentProvider getContentProvider(ScopeTreeViewer treeViewer);
    protected abstract void selectionChanged(IStructuredSelection selection);

    
    /////////////////////////////////////////////////////////
    ///
    ///  classes
    ///
    /////////////////////////////////////////////////////////
    
    
    static class HotCallPath 
    {
    	// last node iterated
    	Scope node = null;
    	
    	TreePath path = null;
    }

    static protected enum ActionType 
    {
    	ZOOM_IN(0), 		ZOOM_OUT(1), 	HOTPATH(2), 
    	DERIVED_METRIC(3), 	EXPORT_DATA(4), COLUMN_HIDE(5),
    	FONT_BIGGER(6), 	FONT_SMALLER(7); 
    	
    	int ord;
    	
    	ActionType(int ord) {
    		this.ord = ord;
    	}
    	
    	int getValue() {
    		return ord;
    	}
    }
}
