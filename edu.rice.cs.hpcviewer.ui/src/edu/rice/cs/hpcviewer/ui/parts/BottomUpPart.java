package edu.rice.cs.hpcviewer.ui.parts;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.internal.BottomUpContentViewer;
import edu.rice.cs.hpcviewer.ui.parts.editor.ViewEventHandler;

public class BottomUpPart implements IBaseView
{
	static public final String ID = "edu.rice.cs.hpcviewer.ui.part.bottomup";

	@Inject EPartService  partService;
	@Inject EModelService modelService;
	@Inject MApplication  app;
	@Inject IEventBroker  broker;
	
	@Inject DatabaseCollection databaseAddOn;

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

		contentViewer = new BottomUpContentViewer(partService, modelService, app, broker, databaseAddOn);
    	contentViewer.createContent(parent);
		
		if (!databaseAddOn.isEmpty()) {
			setExperiment(databaseAddOn.getLast());
		}
	}

	@PreDestroy
	public void preDestroy() {
		eventHandler.dispose();
	}

	@Override
	public void setExperiment(BaseExperiment experiment) {

		this.experiment = (Experiment) experiment;
		
		RootScope rootCCT  = experiment.getRootScope(RootScopeType.CallingContextTree);
		RootScope rootCall = experiment.getRootScope(RootScopeType.CallerTree);
		
		root = this.experiment.createCallersView(rootCCT, rootCall);
		
		contentViewer.setData(root);
	}

	@Override
	public String getViewType() {
		return Experiment.TITLE_BOTTOM_UP_VIEW;
	}

	@Override
	public String getID() {
		return ID;
	}
}
