package edu.rice.cs.hpcviewer.ui.parts.flat;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.ToolBar;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.internal.AbstractTableView;

public class FlatPart extends AbstractTableView 
{

	public FlatPart(CTabFolder parent, int style) {
		super(parent, style, "Flat view");
	}


	@Override
	protected void beginToolbar(CoolBar coolbar, ToolBar toolbar) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void endToolbar(CoolBar coolbar, ToolBar toolbar) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateStatus() {
		// TODO Auto-generated method stub

	}

	@Override
	protected RootScope createRoot() {
		Experiment experiment = (Experiment) getMetricManager();
		RootScope rootCCT  = experiment.getRootScope(RootScopeType.CallingContextTree);
		RootScope rootFlat = experiment.getRootScope(RootScopeType.Flat);
		
		if (rootCCT != null && rootFlat != null)
			return ((Experiment) experiment).createFlatView(rootCCT, rootFlat);

		if (rootFlat != null && rootFlat.hasChildren())
			return rootFlat;
		
		return null;
	}


	@Override
	public RootScopeType getRootType() {
		return RootScopeType.Flat;
	}

}
