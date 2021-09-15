package edu.rice.cs.hpcviewer.ui.parts.bottomup;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.ToolBar;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.internal.AbstractTableView;

public class BottomUpPart extends AbstractTableView 
{
	private static final String TITLE = "Bottom-up view";

	public BottomUpPart(CTabFolder parent, int style) {
		super(parent, style, TITLE);
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
	

	@Override
	protected RootScope createRoot() {
		IMetricManager mm = getMetricManager();
		Experiment experiment = (Experiment) mm;
		
		RootScope rootCCT  = experiment.getRootScope(RootScopeType.CallingContextTree);
		RootScope rootCall = experiment.getRootScope(RootScopeType.CallerTree);
		
		RootScope root = ((Experiment) experiment).createCallersView(rootCCT, rootCall);
		return root;
	}

	@Override
	public RootScopeType getRootType() {
		return RootScopeType.CallerTree;
	}
}
