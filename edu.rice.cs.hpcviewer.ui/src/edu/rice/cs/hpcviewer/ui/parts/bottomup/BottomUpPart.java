package edu.rice.cs.hpcviewer.ui.parts.bottomup;

import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.parts.BaseViewPart;
import edu.rice.cs.hpcviewer.ui.parts.IViewBuilder;

public class BottomUpPart extends BaseViewPart
{
	static final public String IDdesc = "edu.rice.cs.hpcviewer.ui.partdescriptor.bottomup";

	
	public BottomUpPart() {
	}

	@Override
	protected IViewBuilder setContentViewer(Composite parent, EMenuService menuService) {

		IViewBuilder contentViewer = new BottomUpContentViewer(partService, modelService, app, broker, databaseAddOn, partFactory);
    	contentViewer.createContent(parent, menuService);
		return contentViewer;
	}

	@Override
	protected RootScopeType getRootType() {
		return RootScopeType.CallerTree;
	}


	@Override
	protected RootScope createRoot(BaseExperiment experiment) {
		System.out.println(getClass().getSimpleName() + " create root " );
		
		RootScope rootCCT  = experiment.getRootScope(RootScopeType.CallingContextTree);
		RootScope rootCall = experiment.getRootScope(RootScopeType.CallerTree);
		
		RootScope root = ((Experiment) experiment).createCallersView(rootCCT, rootCall);
		return root;
	}
}
