package edu.rice.cs.hpcviewer.ui.parts.topdown;

import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.parts.BaseViewPart;
import edu.rice.cs.hpcviewer.ui.parts.IViewBuilder;

public class TopDownPart extends BaseViewPart
{

	public TopDownPart() {}

	@Override
	protected IViewBuilder setContentViewer(Composite parent, EMenuService menuService) {
		IViewBuilder contentViewer = new TopDownContentViewer(partService, modelService, app, eventBroker, databaseAddOn, partFactory);
    	contentViewer.createContent(parent, menuService);
		return contentViewer;
	}

	@Override
	protected RootScopeType getRootType() {
		return RootScopeType.CallingContextTree;
	}

	@Override
	protected RootScope createRoot(BaseExperiment experiment) {

		// for top-down tree, we don't need to create the tree
		// the tree is already in experiment.xml. 
		
		return experiment.getRootScope(RootScopeType.CallingContextTree);
	}
}
