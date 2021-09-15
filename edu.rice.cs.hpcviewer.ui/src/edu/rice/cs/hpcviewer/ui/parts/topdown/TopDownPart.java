package edu.rice.cs.hpcviewer.ui.parts.topdown;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.ToolBar;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.internal.AbstractTableView;

public class TopDownPart extends AbstractTableView 
{	
	private static final String TITLE = "Top-down view";
	
	public TopDownPart(CTabFolder parent, int style) {
		super(parent, style, TITLE);
	}
	

	@Override
    protected void beginToolbar(CoolBar coolbar, ToolBar toolbar) {}
	
	@Override
    protected void endToolbar  (CoolBar coolbar, ToolBar toolbar) {}


	protected void updateStatus() {
	}


	@Override
	protected RootScope createRoot() {
		IMetricManager mm = getMetricManager();
		Experiment exp = (Experiment) mm;
		
		return exp.getRootScope(getRootType());
	}


	@Override
	public RootScopeType getRootType() {
		return RootScopeType.CallingContextTree;
	}
}
