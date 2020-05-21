package edu.rice.cs.hpcviewer.ui.experiment;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Singleton;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;

@Creatable
@Singleton
public class ExperimentAddOn 
{
	static final public String EVENT_HPC_NEW_DATABASE = "hpcviewer/database";

	private ConcurrentLinkedQueue<BaseExperiment> queueExperiment;
	
	public ExperimentAddOn() {
		queueExperiment = new ConcurrentLinkedQueue<>();
	}
	
	public void addDatabase(BaseExperiment experiment, 
			MApplication application, 
			IEclipseContext context,
			IEventBroker broker,
			EModelService modelService) {
		
		queueExperiment.add(experiment);
		
		if (context == null)
			return;
		
		context.set(ExperimentAddOn.EVENT_HPC_NEW_DATABASE, experiment);
		
		if (broker.post(ExperimentAddOn.EVENT_HPC_NEW_DATABASE, experiment)) {
			if (application != null && modelService != null) {
				MWindow window = (MWindow) modelService.find("edu.rice.cs.hpcviewer.window.main", application);
				window.setLabel("hpcviewer - " + experiment.getDefaultDirectory().getPath());
			}
		}
	}
	
	public int getNumDatabase() {
		return queueExperiment.size();
	}
	
	public boolean isEmpty() {
		return queueExperiment.isEmpty();
	}
	
	public BaseExperiment getLast() {
		return queueExperiment.element();
	}
}
