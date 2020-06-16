 
package edu.rice.cs.hpcviewer.ui.parts;

import javax.inject.Inject;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.internal.TopDownContentViewer;

public class Datacentric  extends BaseViewPart
{
	static final public String IDdesc = "edu.rice.cs.hpcviewer.ui.partdescriptor.datacentric";

	@Inject
	public Datacentric() {
	}
	

	@Override
	protected RootScope createRoot(BaseExperiment experiment) {

		return experiment.getRootScope(RootScopeType.DatacentricTree);
	}

	@Override
	protected IViewBuilder setContentViewer(Composite parent, EMenuService menuService) {

		IViewBuilder contentViewer = new TopDownContentViewer(partService, modelService, app, broker, databaseAddOn, partFactory);
    	contentViewer.createContent(parent, menuService);

    	return contentViewer;
	}

	@Override
	protected RootScopeType getRootType() {

		return RootScopeType.DatacentricTree;
	}
}