package edu.rice.cs.hpcviewer.ui.parts.bottomup;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.ToolBar;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpctree.BottomUpScopeTreeData;
import edu.rice.cs.hpctree.IScopeTreeData;
import edu.rice.cs.hpcviewer.ui.internal.AbstractTableView;

public class BottomUpPart extends AbstractTableView 
{
	private static final String TITLE = "Bottom-up view";

	public BottomUpPart(CTabFolder parent, int style) {
		super(parent, style, TITLE);
		setToolTipText("A view to display the list of callers");
	}

	@Override
	protected void beginToolbar(CoolBar coolbar, ToolBar toolbar) { /* unused */ }

	@Override
	protected void endToolbar(CoolBar coolbar, ToolBar toolbar) { /* unused */ }

	@Override
	protected void updateStatus() { /* unused */ }
	
	
	private boolean isInitialized = false;
	
	@Override
	protected RootScope buildTree(boolean reset) {
		Experiment experiment = (Experiment) getMetricManager();		
		RootScope rootCCT  = experiment.getRootScope(RootScopeType.CallingContextTree);
		RootScope rootCall = experiment.getRootScope(RootScopeType.CallerTree);
		
		if (rootCCT == null || rootCall == null)
			return null;
		
		if (isInitialized && !reset)
			return rootCall;
		
		experiment.createCallersView(rootCCT, rootCall);
		isInitialized = true;
		
		return rootCall;
	}

	@Override
	public RootScopeType getRootType() {
		return RootScopeType.CallerTree;
	}

	@Override
	protected IScopeTreeData getTreeData(RootScope root, IMetricManager metricManager) {
		return new BottomUpScopeTreeData(root, metricManager);
	}

	@Override
	public RootScope getRoot() {
		IMetricManager mm = getMetricManager();
		Experiment experiment = (Experiment) mm;
		return experiment.getRootScope(RootScopeType.CallerTree);
	}
}
