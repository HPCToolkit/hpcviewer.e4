package edu.rice.cs.hpcviewer.ui.parts;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.internal.TopDownContentViewer;
import edu.rice.cs.hpcviewer.ui.parts.editor.ViewEventHandler;

public class TopDownPart implements IBaseView
{
	static final public String ID = "edu.rice.cs.hpcviewer.ui.part.topdown";

	@Inject EPartService partService;
	@Inject EModelService modelService;
	@Inject MApplication  app;

	@Inject IEventBroker broker;
	@Inject DatabaseCollection databaseAddOn;

	private ViewEventHandler eventHandler;
	private IContentViewer   contentViewer;

	public TopDownPart() {
	}

    @PostConstruct
    public void createControls(Composite parent, EMenuService menuService) {
		eventHandler = new ViewEventHandler(this, broker, partService);
		
    	contentViewer = new TopDownContentViewer(partService, modelService, app, broker, databaseAddOn);
    	contentViewer.createContent(parent, menuService);
		
		if (!databaseAddOn.isEmpty()) {
			setExperiment(databaseAddOn.getLast());
		}
    }
    
	@PreDestroy
	public void preDestroy() {
		eventHandler.dispose();
		contentViewer.dispose();
	}

	@Override
	public void setExperiment(BaseExperiment experiment) {
		contentViewer.setData(experiment.getRootScope(RootScopeType.CallingContextTree));
	}

	@Override
	public String getViewType() {
		return Experiment.TITLE_TOP_DOWN_VIEW;
	}

	@Override
	public String getID() {
		return ID;
	}
}
