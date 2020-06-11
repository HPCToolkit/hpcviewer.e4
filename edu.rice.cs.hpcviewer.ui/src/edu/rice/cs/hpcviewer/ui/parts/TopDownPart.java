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
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.internal.TopDownContentViewer;

public class TopDownPart extends BaseViewPart
{
	static final public String ID = "edu.rice.cs.hpcviewer.ui.part.topdown";
	static final public String IDdesc = "edu.rice.cs.hpcviewer.ui.partdescriptor.topdown";

	@Inject EPartService partService;
	@Inject EModelService modelService;
	@Inject MApplication  app;

	@Inject IEventBroker broker;
	@Inject DatabaseCollection databaseAddOn;

	public TopDownPart() {}

	@Override
	public String getViewType() {
		return Experiment.TITLE_TOP_DOWN_VIEW;
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	protected IContentViewer setContentViewer(Composite parent, EMenuService menuService) {
		IContentViewer contentViewer = new TopDownContentViewer(partService, modelService, app, broker, databaseAddOn);
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
