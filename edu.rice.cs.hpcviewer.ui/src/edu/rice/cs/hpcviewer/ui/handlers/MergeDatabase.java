 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.merge.ExperimentMerger;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.parts.editor.Editor;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.CanExecute;

public class MergeDatabase 
{
	static final private String PARAM_ID = "edu.rice.cs.hpcviewer.ui.commandparameter.merge";
	
	@Inject DatabaseCollection database;
	
	@Execute
	public void execute(@Active Shell shell, @Optional @Named(PARAM_ID) String param,
			MApplication application,
			EPartService service,
			IEventBroker broker,
			EModelService modelService) {
		
		System.out.println("merge database: " + param);
		
		final Experiment []db = new Experiment[2];

		if (database.getNumDatabase()==2) {
			Iterator<BaseExperiment> iterator = database.getIterator();
			int i = 0;
			while(iterator.hasNext()) {
				db[i] = (Experiment) iterator.next();
				i++;
			}
		} else {
			String msg = "hpcviewer currently does not support merging more than two databases";
			System.out.println(msg);
			MessageDialog.openError(shell, "Unsupported action", msg);
			return;
		}
		
		final RootScopeType mergeType;
		if (param.equals("topdown")) {
			mergeType = RootScopeType.CallingContextTree;
		} else if (param.equals("flat")) {
			mergeType = RootScopeType.Flat;
		} else {
			System.err.println("unknown merge type");
			return;
		}
		
		final Display display = shell.getDisplay();
		display.asyncExec(new Runnable() {
			
			@Override
			public void run() {
				try {
					Experiment mergedExp = ExperimentMerger.merge(db[0], db[1], mergeType);
					database.createViewsAndAddDatabase(mergedExp, application, service, modelService, Editor.STACK_ID);
					
				} catch (Exception e) {
					MessageDialog.openError(shell, "Error merging database",
							e.getMessage());
				}
				
			}
		});
	}
	
	
	@CanExecute
	public boolean canExecute() {
		
		// temporarily, we don't supportt merging more than 2 databases
		return database.getNumDatabase()==2;
	}
		
}