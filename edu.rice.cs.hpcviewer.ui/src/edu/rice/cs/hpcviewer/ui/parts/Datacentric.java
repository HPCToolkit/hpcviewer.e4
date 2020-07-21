 
package edu.rice.cs.hpcviewer.ui.parts;

import javax.inject.Inject;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.parts.topdown.TopDownContentViewer;
import edu.rice.cs.hpcviewer.ui.tabItems.AbstractViewItem;

public class Datacentric extends AbstractViewItem
{

	@Inject
	public Datacentric(CTabFolder parent, int style) {
		super(parent, style);
	}
	

	@Override
	protected RootScope createRoot(BaseExperiment experiment) {

		return experiment.getRootScope(RootScopeType.DatacentricTree);
	}

	@Override
	protected IViewBuilder setContentViewer(Composite parent, EMenuService menuService) {

		IViewBuilder contentViewer = new TopDownContentViewer(partService, eventBroker, databaseAddOn, null);
    	contentViewer.createContent(parent, menuService);

    	return contentViewer;
	}

	@Override
	protected RootScopeType getRootType() {

		return RootScopeType.DatacentricTree;
	}
}