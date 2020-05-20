package edu.rice.cs.hpcviewer.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpcviewer.experiment.ExperimentAddOn;
import edu.rice.cs.hpcviewer.experiment.ExperimentManager;

public class FileOpenDatabase 
{
	static public final String EDITOR_ID = "edu.rice.cs.hpcviewer.ui.part.editor";
	
	@Inject EPartService partService;
	@Inject IEventBroker broker;
	@Inject MApplication application;
	@Inject EModelService modelService;
	
	@Inject ExperimentAddOn experimentManager;

	@Execute
	public void execute(IWorkbench workbench, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		
		ExperimentManager expManager = new ExperimentManager();
		BaseExperiment experiment    = expManager.openFileExperiment(shell);
		
		if (experiment == null)
			return;
		
		IEclipseContext context = application.getContext();
		
		Object obj = context.get(ExperimentAddOn.EVENT_HPC_NEW_DATABASE);
		if (obj != null) {
		}
		experimentManager.addDatabase(experiment);
		
		context.set(ExperimentAddOn.EVENT_HPC_NEW_DATABASE, experiment);
		
		if (broker.post(ExperimentAddOn.EVENT_HPC_NEW_DATABASE, experiment)) {
			MWindow window = (MWindow) modelService.find("edu.rice.cs.hpcviewer.window.main", application);
			window.setLabel("hpcviewer - " + experiment.getDefaultDirectory().getPath());
		}
	}
}
