package edu.rice.cs.hpcviewer.ui.parts;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.IPartListener;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.experiment.ExperimentAddOn;
import edu.rice.cs.hpcviewer.ui.internal.BaseContentViewer;
import edu.rice.cs.hpcviewer.ui.parts.editor.ViewEventHandler;

public class BottomUpPart implements IBaseView, IPartListener
{
	static public final String ID = "edu.rice.cs.hpcviewer.ui.part.bottomup";

	@Inject EPartService partService;
	@Inject IEventBroker broker;
	@Inject ExperimentAddOn databaseAddOn;

	private ViewEventHandler eventHandler;
	private IContentViewer   contentViewer;

	private Experiment experiment;

	private RootScope root;

	public BottomUpPart() {
		root = null;
		experiment = null;
	}

	@PostConstruct
    public void createControls(Composite parent) {
		
		eventHandler = new ViewEventHandler(this, broker, partService);

		contentViewer = new BaseContentViewer();
    	contentViewer.createContent(parent);
    	
		partService.addPartListener(this);
		
		if (!databaseAddOn.isEmpty()) {
			setExperiment(databaseAddOn.getLast());
		}
	}

	@PreDestroy
	public void preDestroy() {
		eventHandler.dispose();
		partService.removePartListener(this);
	}

	@Override
	public void setExperiment(BaseExperiment experiment) {
		root = null;
		this.experiment = (Experiment) experiment;
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
	public void partActivated(MPart part) {}

	@Override
	public void partBroughtToTop(MPart part) {}

	@Override
	public void partDeactivated(MPart part) {}

	@Override
	public void partHidden(MPart part) {}

	@Override
	public void partVisible(MPart part) {
		if (!part.getElementId().equals(ID))
			return;
		
		if (experiment == null) return;
		
		if (root != null) return;
		
		RootScope rootCCT  = experiment.getRootScope(RootScopeType.CallingContextTree);
		RootScope rootCall = experiment.getRootScope(RootScopeType.CallerTree);
		
		root = experiment.createCallersView(rootCCT, rootCall);
		
		contentViewer.setData(root);
	}

}
