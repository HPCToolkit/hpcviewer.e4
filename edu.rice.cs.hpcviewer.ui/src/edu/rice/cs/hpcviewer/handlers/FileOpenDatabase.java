package edu.rice.cs.hpcviewer.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpcviewer.experiment.ExperimentAddOn;
import edu.rice.cs.hpcviewer.experiment.ExperimentManager;
import edu.rice.cs.hpcviewer.ui.parts.BottomUpPart;
import edu.rice.cs.hpcviewer.ui.parts.FlatPart;
import edu.rice.cs.hpcviewer.ui.parts.TopDownPart;

public class FileOpenDatabase 
{
	static public final String EDITOR_ID = "edu.rice.cs.hpcviewer.ui.part.editor";
	
	@Inject EPartService partService;
	@Inject IEventBroker broker;

	@Execute
	public void execute(IWorkbench workbench, Shell shell) {
		System.out.println(getClass().getSimpleName() + " called");
		
		ExperimentManager expManager = new ExperimentManager();
		BaseExperiment experiment    = expManager.openFileExperiment(shell);
		
		if (experiment == null)
			return;
		
		partService.showPart(FlatPart.ID, PartState.CREATE);
		partService.showPart(BottomUpPart.ID, PartState.CREATE);
		partService.showPart(TopDownPart.ID, PartState.VISIBLE);
		partService.showPart(TopDownPart.ID, PartState.ACTIVATE);
		
		if (broker.post(ExperimentAddOn.EVENT_HPC_NEW_DATABASE, experiment)) {
		}
	}
}
