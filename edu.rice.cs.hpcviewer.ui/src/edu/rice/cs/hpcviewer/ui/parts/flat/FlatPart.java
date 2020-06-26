package edu.rice.cs.hpcviewer.ui.parts.flat;

import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.parts.BaseViewPart;
import edu.rice.cs.hpcviewer.ui.parts.IViewBuilder;

public class FlatPart extends BaseViewPart
{
	public FlatPart() {	}


	@Override
	protected RootScope createRoot(BaseExperiment experiment) {

		RootScope rootCCT  = experiment.getRootScope(RootScopeType.CallingContextTree);
		RootScope rootFlat = experiment.getRootScope(RootScopeType.Flat);
		
		if (rootCCT != null && rootFlat != null)
			return ((Experiment) experiment).createFlatView(rootCCT, rootFlat);

		if (rootFlat != null && rootFlat.hasChildren())
			return rootFlat;
		
		return null;
	}

	@Override
	protected IViewBuilder setContentViewer(Composite parent, EMenuService menuService) {

		IViewBuilder contentViewer = new FlatContentViewer(partService, eventBroker, databaseAddOn, partFactory);
    	contentViewer.createContent(parent, menuService);
		return contentViewer;
	}

	@Override
	protected RootScopeType getRootType() {
		// TODO Auto-generated method stub
		return null;
	}

}
