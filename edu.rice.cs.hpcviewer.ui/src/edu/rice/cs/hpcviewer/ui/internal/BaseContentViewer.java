package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpcviewer.ui.parts.IContentViewer;
import edu.rice.cs.hpcviewer.ui.resources.IconManager;
import edu.rice.cs.hpcviewer.ui.util.Utilities;

public abstract class BaseContentViewer implements IContentViewer 
{
	final int TREE_COLUMN_WIDTH  = 200;
	
	private ScopeTreeViewer treeViewer = null;
	private Scope nodeTopParent = null;

	@Override
	public void createContent(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		GridData gd_composite = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_composite.widthHint = 506;
		composite.setLayoutData(gd_composite);
				
		CoolBar coolBar = new CoolBar(composite, SWT.FLAT);

		ToolBar toolBar = new ToolBar(coolBar, SWT.FLAT | SWT.RIGHT);

		// add the beginning toolbar
		beginToolbar(coolBar, toolBar);
		
		createToolItem(toolBar, IconManager.Image_ZoomIn,    "Zoom-in the selected node");
		createToolItem(toolBar, IconManager.Image_ZoomOut,   "Zoom-out the selected node");
		createToolItem(toolBar, IconManager.Image_FlameIcon, "Expand the hot path below the selected node");

		createToolItem(toolBar, IconManager.Image_FnMetric,     "Add a new derived metric");
		createToolItem(toolBar, IconManager.Image_CheckColumns, "Hide/show columns");
		createToolItem(toolBar, IconManager.Image_SaveCSV,  	"Export the current view into a comma separated value file");

		createToolItem(toolBar, IconManager.Image_FontBigger,  "Increase font size");
		createToolItem(toolBar, IconManager.Image_FontSmaller, "Decrease font size");

		// add the end toolbar
		endToolbar(coolBar, toolBar);

		createCoolItem(coolBar, toolBar);
		
		Point p = coolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		p.x += 20;
		
		coolBar.setSize(p);
		
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, false).applyTo(coolBar);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(coolBar);

		treeViewer = new ScopeTreeViewer(parent, SWT.BORDER);
		Tree tree = treeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

		treeViewer.setContentProvider( getContentProvider(treeViewer));
		
		TreeViewerColumn colViewer =  new TreeViewerColumn(treeViewer, SWT.LEFT);
		colViewer.getColumn().setWidth(TREE_COLUMN_WIDTH);
		colViewer.getColumn().setText ("Scope");
		colViewer.setLabelProvider    (new StyledScopeLabelProvider());
	}

	@Override
	public void setData(RootScope root) {
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
		insertParentNode(root);
		
		// resize the width of metric columns
		TreeColumn columns[] = treeViewer.getTree().getColumns();
		for(TreeColumn col:columns) {
			if (col.getData() != null) {
				col.pack();
			}
		}
	}
	
	protected Scope getFirstRowNode() {
		return nodeTopParent;
	}
	
	protected ToolItem createToolItem(ToolBar toolbar, int toolbarStyle, String name, String tooltip) {
		
		IconManager iconManager = IconManager.getInstance();
		ToolItem toolitem = new ToolItem(toolbar, toolbarStyle);
		
		toolitem.setImage(iconManager.getImage(name));
		toolitem.setToolTipText(tooltip);
		toolitem.setEnabled(false);
		
		return toolitem;
	}
	
	protected ToolItem createToolItem(ToolBar toolbar, String name, String tooltip) {
				
		return createToolItem(toolbar, SWT.PUSH, name, tooltip);
	}

    protected abstract void beginToolbar(CoolBar coolbar, ToolBar toolbar);
    protected abstract void endToolbar(CoolBar coolbar, ToolBar toolbar);
    protected abstract AbstractContentProvider getContentProvider(ScopeTreeViewer treeViewer);
	
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
     * Inserting a "node header" on the top of the table to display
     * either aggregate metrics or "parent" node (due to zoom-in)
     * TODO: we need to shift to the left a little bit
     * @param nodeParent
     */
    private void insertParentNode(Scope nodeParent) {
    	Scope scope = nodeParent;
    	
    	// Bug fix: avoid using list of columns from the experiment
    	// formerly: .. = this.myExperiment.getMetricCount() + 1;
    	TreeColumn []columns = treeViewer.getTree().getColumns();
    	int nbColumns = columns.length; 	// columns in base metrics
    	String []sText = new String[nbColumns];
    	sText[0] = new String(scope.getName());
    	
    	// --- prepare text for base metrics
    	// get the metrics for all columns
    	for (int i=1; i< nbColumns; i++) {
    		// we assume the column is not null
    		Object o = columns[i].getData();
    		if(o instanceof BaseMetric) {
    			BaseMetric metric = (BaseMetric) o;
    			// ask the metric for the value of this scope
    			// if it's a thread-level metric, we will read metric-db file
    			sText[i] = metric.getMetricTextValue(scope);
    		}
    	}
    	
    	// draw the root node item
    	Utilities.insertTopRow(treeViewer, Utilities.getScopeNavButton(scope), sText);
    	this.nodeTopParent = nodeParent;
    }
}
