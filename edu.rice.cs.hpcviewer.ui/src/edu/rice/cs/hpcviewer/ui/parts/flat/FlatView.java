package edu.rice.cs.hpcviewer.ui.parts.flat;

import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.parts.AbstractViewItem;
import edu.rice.cs.hpcviewer.ui.parts.IViewBuilder;

public class FlatView extends AbstractViewItem {

	public FlatView(CTabFolder parent, int style) {
		super(parent, style);
		setText("Flat view");
		setToolTipText("A view to display the static structure of the application and its metrics");
	}

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

		return new FlatContentViewer(partService, eventBroker, databaseAddOn, profilePart);
	}

	@Override
	protected RootScopeType getRootType() {
		return RootScopeType.Flat;
	}

}
