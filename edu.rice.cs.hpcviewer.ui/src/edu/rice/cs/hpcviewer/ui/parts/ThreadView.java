package edu.rice.cs.hpcviewer.ui.parts;

import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

import edu.rice.cs.hpcviewer.ui.parts.thread.ThreadContentViewer;

public class ThreadView extends BaseViewPart
{
	static public final String IDdesc = "edu.rice.cs.hpcviewer.ui.partdescriptor.thread";

	@Override
	public String getViewType() {
		return "Thread view";
	}

	@Override
	public String getID() {
		return IDdesc;
	}

	@Override
	protected RootScope createRoot(BaseExperiment experiment) {

		// create and duplicate the configuration
		RootScope rootCCT    = experiment.getRootScope(RootScopeType.CallingContextTree);
		RootScope rootThread = (RootScope) rootCCT.duplicate();
		rootThread.setRootName("Thread View");
		
		// duplicate the children
		for(int i=0; i<rootCCT.getChildCount(); i++)
		{
			Scope scope = (Scope) rootCCT.getChildAt(i);
			rootThread.addSubscope(scope);
		}
		return rootThread;
	}

	@Override
	protected IContentViewer setContentViewer(Composite parent, EMenuService menuService) {
		IContentViewer contentViewer = new ThreadContentViewer(partService, modelService, app, broker, databaseAddOn, partFactory);
    	contentViewer.createContent(parent, menuService);
		return contentViewer;
	}

	@Override
	protected RootScopeType getRootType() {

		return RootScopeType.Unknown;
	}

	
}
