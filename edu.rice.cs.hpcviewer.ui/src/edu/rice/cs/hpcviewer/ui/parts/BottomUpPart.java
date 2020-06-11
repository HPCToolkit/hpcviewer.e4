package edu.rice.cs.hpcviewer.ui.parts;

import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.internal.BottomUpContentViewer;

public class BottomUpPart extends BaseViewPart
{
	static public final String ID = "edu.rice.cs.hpcviewer.ui.part.bottomup";
	static final public String IDdesc = "edu.rice.cs.hpcviewer.ui.partdescriptor.bottomup";

	@Inject EPartService  partService;
	@Inject EModelService modelService;
	@Inject MApplication  app;
	@Inject IEventBroker  broker;
	
	public BottomUpPart() {
	}

	@Override
	public String getViewType() {
		return Experiment.TITLE_BOTTOM_UP_VIEW;
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	protected IContentViewer setContentViewer(Composite parent, EMenuService menuService) {

		IContentViewer contentViewer = new BottomUpContentViewer(partService, modelService, app, broker, databaseAddOn);
    	contentViewer.createContent(parent, menuService);
		return contentViewer;
	}

	@Override
	protected RootScopeType getRootType() {
		return RootScopeType.CallerTree;
	}


	@Override
	protected RootScope createRoot(BaseExperiment experiment) {

		RootScope rootCCT  = experiment.getRootScope(RootScopeType.CallingContextTree);
		RootScope rootCall = experiment.getRootScope(RootScopeType.CallerTree);
		
		RootScope root = ((Experiment) experiment).createCallersView(rootCCT, rootCall);
		return root;
	}
}
