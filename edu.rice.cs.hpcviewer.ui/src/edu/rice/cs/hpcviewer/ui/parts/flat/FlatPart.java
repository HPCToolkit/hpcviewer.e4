package edu.rice.cs.hpcviewer.ui.parts.flat;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.ToolBar;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpctree.IScopeTreeData;
import edu.rice.cs.hpctree.ScopeTreeData;
import edu.rice.cs.hpcviewer.ui.internal.AbstractTableView;

public class FlatPart extends AbstractTableView 
{

	public FlatPart(CTabFolder parent, int style) {
		super(parent, style, "Flat view");
	}


	@Override
	protected void beginToolbar(CoolBar coolbar, ToolBar toolbar) {
	}

	
	@Override
	protected void endToolbar(CoolBar coolbar, ToolBar toolbar) {
	}
	

	@Override
	protected void updateStatus() {
	}

	
	private boolean isInitialized = false;
	
	@Override
	protected RootScope createRoot() {
		Experiment experiment = (Experiment) getMetricManager();
		RootScope rootCCT  = experiment.getRootScope(RootScopeType.CallingContextTree);
		RootScope rootFlat = experiment.getRootScope(RootScopeType.Flat);
		
		if (rootFlat == null || rootCCT == null)
			return null;
		
		if (isInitialized)
			return rootFlat;
		
		isInitialized = true;
		return ((Experiment) experiment).createFlatView(rootCCT, rootFlat);
	}


	@Override
	public RootScopeType getRootType() {
		return RootScopeType.Flat;
	}


	@Override
	protected IScopeTreeData getTreeData(RootScope root, IMetricManager metricManager) {
		return new ScopeTreeData(root, metricManager);
	}

}
