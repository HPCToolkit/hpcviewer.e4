package edu.rice.cs.hpcviewer.ui.parts;

import org.eclipse.e4.ui.services.EMenuService;
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
		System.out.println(getClass().getSimpleName() + " create root " );
		
		RootScope rootCCT  = experiment.getRootScope(RootScopeType.CallingContextTree);
		RootScope rootCall = experiment.getRootScope(RootScopeType.CallerTree);
		
		RootScope root = ((Experiment) experiment).createCallersView(rootCCT, rootCall);
		return root;
	}
}
