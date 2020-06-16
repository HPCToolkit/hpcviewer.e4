 
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
import edu.rice.cs.hpcviewer.ui.parts.editor.PartFactory;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.IPartListener;

public abstract class BaseViewPart implements IBaseView, EventHandler, IPartListener
{

	@Inject EPartService partService;
	@Inject EModelService modelService;
	@Inject MApplication  app;

	@Inject IEventBroker broker;
	@Inject DatabaseCollection databaseAddOn;

	@Inject PartFactory partFactory;

	private IContentViewer  contentViewer;
	
	/** Each view needs to store the experiment database.
	 * In case it needs to populate the table, we know which database 
	 * to be loaded. */
	private BaseExperiment  experiment;
	
	/** This variable is a flag whether a table is already populated or not.
	 * If the root is null, it isn't populated
	 */
	private RootScope       root;

	@Inject
	public BaseViewPart() {
	}
	
	@PostConstruct
	public void postConstruct(Composite parent, EMenuService menuService) {

		contentViewer = setContentViewer(parent, menuService);
		
		partService.addPartListener(this);
	}
	
	@PreDestroy
	public void preDestroy() {
		
		if (contentViewer != null)
			contentViewer.dispose();
	}
	

	@Override
	public void setInput(MPart part, Object input) {
		
		// important: needs to store the experiment database for further usage
		// when the view is becoming visible
		this.experiment = (BaseExperiment) input;
		
		if (partService.isPartVisible(part)) {
			
			root = createRoot(experiment);
			contentViewer.setData(root);
		}
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
		if (part.getObject() != this)
			return;
		
		if (experiment != null && root == null) {
			root = createRoot(experiment);
			contentViewer.setData(root);
		}
	}


	protected abstract RootScope      createRoot(BaseExperiment experiment);
	protected abstract IContentViewer setContentViewer(Composite parent, EMenuService menuService);
	protected abstract RootScopeType  getRootType();
	
}