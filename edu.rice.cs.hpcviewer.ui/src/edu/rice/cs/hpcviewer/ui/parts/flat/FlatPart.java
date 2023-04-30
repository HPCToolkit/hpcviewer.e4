package edu.rice.cs.hpcviewer.ui.parts.flat;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpctree.FlatScopeTreeData;
import edu.rice.cs.hpctree.IScopeTreeData;
import edu.rice.cs.hpctree.action.FlatAction;
import edu.rice.cs.hpcviewer.ui.internal.AbstractTableView;
import edu.rice.cs.hpcviewer.ui.resources.IconManager;

public class FlatPart extends AbstractTableView 
{
	private static final int ITEM_FLAT = 0;
	private static final int ITEM_UNFLAT = 1;
	
	private FlatAction flatAction;
	private ToolItem[] items;
	private FlatScopeTreeData treeData;

	public FlatPart(CTabFolder parent, int style) {
		super(parent, style, "Flat view");
		setToolTipText("A view to display the static structure of the application and its metrics");
	}


	@Override
	protected void beginToolbar(CoolBar coolbar, ToolBar toolbar) {
		items = new ToolItem[2];
		
		items[ITEM_FLAT]   = createToolItem(toolbar, 
											IconManager.Image_Flatten, 
										    "Flatten nodes one level");
		items[ITEM_UNFLAT] = createToolItem(toolbar, 
											IconManager.Image_Unflatten, 
											"Unflatten nodes one level");
		
		items[ITEM_FLAT].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				flatAction.flatten(getTable().getRoot());
				updateButtonStatus();
			}
		});
		
		items[ITEM_UNFLAT].addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				flatAction.unflatten();
				updateButtonStatus();
			}
		});
	}

	
	@Override
	protected void endToolbar(CoolBar coolbar, ToolBar toolbar) { /* unused */ }

	
	@Override
	public void setInput(Object input) {
		super.setInput(input);
		// we have to initialize flatAction here because we need the value of
		// getTable() which is created after setInput
		flatAction = new FlatAction(getActionManager(), getTable());
		flatAction.setTreeData(treeData);
	}

	@Override
	protected void updateStatus() {
		if (items == null || flatAction == null)
			return;
		
		items[ITEM_FLAT].setEnabled(flatAction.canFlatten());		
		items[ITEM_UNFLAT].setEnabled(flatAction.canUnflatten());
	}

	
	private boolean isInitialized = false;
	@Override
	protected RootScope buildTree(boolean reset) {
		Experiment experiment = (Experiment) getMetricManager();
		RootScope rootCCT  = experiment.getRootScope(RootScopeType.CallingContextTree);
		RootScope rootFlat = experiment.getRootScope(RootScopeType.Flat);
		
		if (rootFlat != null && rootCCT == null)
			return rootFlat;

		if (isInitialized && !reset)
			return rootFlat;
		
		isInitialized = true;
		return experiment.createFlatView(rootCCT, rootFlat);
	}


	@Override
	public RootScopeType getRootType() {
		return RootScopeType.Flat;
	}

	@Override
	public RootScope getRoot() {
		IMetricManager mm = getMetricManager();
		Experiment experiment = (Experiment) mm;
		return experiment.getRootScope(RootScopeType.Flat);
	}


	@Override
	protected IScopeTreeData getTreeData(RootScope root, IMetricManager metricManager) {
		if (treeData == null)
			treeData = new FlatScopeTreeData(root, metricManager);
		return treeData;
	}


	@Override
	public void dispose() {
		super.dispose();
		
		items = null;
		treeData.clear();
		flatAction = null;
	}
}
