package edu.rice.cs.hpcviewer.ui.parts.editor;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpcviewer.ui.experiment.ExperimentAddOn;
import edu.rice.cs.hpcviewer.ui.parts.IBaseView;

public class ViewEventHandler implements EventHandler 
{
	final private IEventBroker broker;
	final private EPartService partService;
	
	private IBaseView view;
	
	public ViewEventHandler(IBaseView view, IEventBroker broker, EPartService partService) {
		this.view   = view;
		this.broker = broker;
		this.partService = partService;
		
		broker.subscribe(ExperimentAddOn.EVENT_HPC_NEW_DATABASE, this);
	}
	
	@Override
	public void handleEvent(Event event) {
		Object obj = event.getProperty(IEventBroker.DATA);
		
		if (obj instanceof Experiment) {
			MPart part = partService.showPart(view.getID(), PartState.VISIBLE);
			
			if (part != null) {
				view.setExperiment((Experiment) obj);
			}
		}
	}
	
	public void dispose() {
		broker.unsubscribe(this);
	}
}
