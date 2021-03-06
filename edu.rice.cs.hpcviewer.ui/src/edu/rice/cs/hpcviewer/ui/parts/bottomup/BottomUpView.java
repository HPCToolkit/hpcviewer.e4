package edu.rice.cs.hpcviewer.ui.parts.bottomup;

import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.base.IViewBuilder;
import edu.rice.cs.hpcviewer.ui.internal.AbstractViewItem;

public class BottomUpView extends AbstractViewItem {

	public BottomUpView(CTabFolder parent, int style) {
		super(parent, style);
		setText("Bottom-up view");
		setToolTipText("A view to display the list of callers");
	}

	@Override
	protected RootScope createRoot(BaseExperiment experiment) {
		
		RootScope rootCCT  = experiment.getRootScope(RootScopeType.CallingContextTree);
		RootScope rootCall = experiment.getRootScope(RootScopeType.CallerTree);
		
		RootScope root = ((Experiment) experiment).createCallersView(rootCCT, rootCall);
		return root;
	}

	@Override
	protected IViewBuilder setContentViewer(Composite parent, EMenuService menuService) {

		return new BottomUpContentViewer(partService, eventBroker, databaseAddOn, profilePart);
	}

	@Override
	protected RootScopeType getRootType() {
		return RootScopeType.CallerTree;
	}

}
