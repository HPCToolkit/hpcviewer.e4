package edu.rice.cs.hpcviewer.ui.actions;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.Dialog;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.DerivedMetric;
import edu.rice.cs.hpc.data.experiment.metric.IMetricManager;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpcviewer.ui.internal.ViewerDataEvent;
import edu.rice.cs.hpcviewer.ui.metric.ExtDerivedMetricDlg;

public class UserDerivedMetric 
{
	final private RootScope 	root;
	final private IEventBroker  eventBroker;

	public UserDerivedMetric(RootScope root, IEventBroker eventBroker) {
		
		this.root 		 = root;
		this.eventBroker = eventBroker;
	}
	
	public void addNewMeric() {
		
		IMetricManager mm = (IMetricManager) root.getExperiment();
		ExtDerivedMetricDlg dialog = new ExtDerivedMetricDlg(null, mm, root);
	
		if (dialog.open() == Dialog.OK) {
			
			final DerivedMetric metric = dialog.getMetric();
			mm.addDerivedMetric(metric);
			
			ViewerDataEvent data = new ViewerDataEvent((Experiment) root.getExperiment(), metric);
			eventBroker.post(ViewerDataEvent.TOPIC_HPC_ADD_NEW_METRIC, data);
		}
	}
}
