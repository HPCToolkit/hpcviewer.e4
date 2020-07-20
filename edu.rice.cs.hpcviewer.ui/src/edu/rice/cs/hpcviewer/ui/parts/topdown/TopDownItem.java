package edu.rice.cs.hpcviewer.ui.parts.topdown;

import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.parts.AbstractViewItem;
import edu.rice.cs.hpcviewer.ui.parts.IViewBuilder;

public class TopDownItem extends AbstractViewItem
{

	public TopDownItem(CTabFolder parent, int style) {
		super(parent, style);
		setText("Top-down");
	}

	


	protected RootScope createRoot(BaseExperiment experiment) {

		// for top-down tree, we don't need to create the tree
		// the tree is already in experiment.xml. 
		
		return experiment.getRootScope(RootScopeType.CallingContextTree);
	}

	@Override
	protected IViewBuilder setContentViewer(Composite parent, EMenuService menuService) {
		return new TopDownContentViewer(partService, eventBroker, databaseAddOn, partFactory);
	}

	@Override
	protected RootScopeType getRootType() {

		return RootScopeType.CallingContextTree;
	}

}
