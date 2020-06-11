 
package edu.rice.cs.hpcviewer.ui.parts;

import javax.inject.Inject;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.swt.widgets.Composite;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public abstract class BaseViewPart implements IBaseView, EventHandler
{
	static final public String ID = "edu.rice.cs.hpcviewer.ui.partdescriptor.basePart";

	@Inject EPartService partService;
	@Inject EModelService modelService;
	@Inject MApplication  app;

	@Inject IEventBroker broker;
	@Inject DatabaseCollection databaseAddOn;

	private IContentViewer   contentViewer;

	@Inject
	public BaseViewPart() {
	}
	
	@PostConstruct
	public void postConstruct(Composite parent, EMenuService menuService) {

		contentViewer = setContentViewer(parent, menuService);
		
		if (!databaseAddOn.isEmpty()) {
			setExperiment(databaseAddOn.getLast());
		}
	}
	
	@PreDestroy
	public void preDestroy() {
		
		if (contentViewer != null)
			contentViewer.dispose();
	}


	@Override
	public void setExperiment(BaseExperiment experiment) {
		
		RootScope root = createRoot(experiment);
		contentViewer.setData(root);
	}

	@Override
	public BaseExperiment getExperiment() {
		RootScope root = contentViewer.getData();
		if (root != null)
			return root.getExperiment();
		
		return null;
	}
	
	@Override
	public void handleEvent(Event event) {
		String topic = event.getTopic();
		System.out.println("event: " + topic);
	}
	
	@Focus
	public void onFocus() {		
	}


	protected abstract RootScope      createRoot(BaseExperiment experiment);
	protected abstract IContentViewer setContentViewer(Composite parent, EMenuService menuService);
	protected abstract RootScopeType  getRootType();
	
}