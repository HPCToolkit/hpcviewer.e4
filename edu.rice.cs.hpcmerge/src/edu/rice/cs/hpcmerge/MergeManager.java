package edu.rice.cs.hpcmerge;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.merge.DatabasesToMerge;
import edu.rice.cs.hpcdata.merge.ExperimentMerger;

public class MergeManager 
{

	/****
	 * Merge databases. If the application has more than two databases,
	 * it will ask the user to select two databases to be merged.
	 * 
	 * @param shell
	 * 			The current shell widget
	 * @param experiments
	 * 			The {@code List} of experiments
	 * @param type
	 * 			The root type to merge. Either {@link RootScopeType.CallingContextTree}
	 * 			or {@link RootScopeType.Flat} 
	 * @param callback
	 * 			The callback object when the merging is done (or error).
	 */
	public static void merge(Shell shell, 
							 List<Experiment> experiments, 
							 RootScopeType type, 
							 IMergeCallback callback) {
		
		// in case there are more than 2 databases: we select two databases to merge and its metric
		// case for exactly 2 databases: just select the metric to compare
		DatabaseMergeWizard dmw = new DatabaseMergeWizard(experiments);
		WizardDialog dialog = new WizardDialog(shell, dmw);
		
		if (dialog.open() == Dialog.CANCEL) 
			return;
		
		final DatabasesToMerge dm = dmw.getDatabaseToMerge();
		
		// check the type of merging: top-down or flat. 
		// bottom up is not supported yet
		dm.type = type;	
		
		BusyIndicator.showWhile(shell.getDisplay(), () -> {
			try {
				Experiment mergedExp = ExperimentMerger.merge(dm);
				callback.mergeDone(mergedExp);
			} catch (Exception e) {
				callback.mergeError(e.getMessage());
			}
		});
	}
	
	
	/****
	 * 
	 * Callback after the merging. It can be either successful or error or cancel.
	 * At the moment, no callback in case of cancel. 
	 *
	 */
	public interface IMergeCallback
	{
		/***
		 * Callback when the merging is successful. 
		 * @param experiment 
		 * 			the merged database
		 */
		void mergeDone(Experiment experiment);
		
		/***
		 * Callback when an error occurs
		 * @param errorMsg
		 * 			The reason of the error
		 */
		void mergeError(String errorMsg);
	}
}
