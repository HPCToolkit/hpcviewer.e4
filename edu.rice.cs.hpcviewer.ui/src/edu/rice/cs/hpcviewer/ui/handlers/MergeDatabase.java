 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.merge.ExperimentMerger;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.CanExecute;

public class MergeDatabase 
{
	static final private String PARAM_ID = "edu.rice.cs.hpcviewer.ui.commandparameter.merge";
	
	static final private String PARAM_VALUE_TOPDOWN = "topdown";
	static final private String PARAM_VALUE_FLAT    = "flat";
	
	@Inject DatabaseCollection database;
	
	@Execute
	public void execute(@Active Shell shell, @Optional @Named(PARAM_ID) String param,
			MApplication application,
			EPartService service,
			IEventBroker broker,
			EModelService modelService) {
		
		// ---------------------------------------------------------------
		// gather 2 databases to be merged.
		// we don't want to merge an already merged database. Skip it.
		// ---------------------------------------------------------------
		final Experiment []db = new Experiment[2];
		
		Iterator<BaseExperiment> iterator = database.getIterator();
		int numDb = 0;
		while(iterator.hasNext()) {
			Experiment exp = (Experiment) iterator.next();
			if (!exp.isMergedDatabase()) {
				db[numDb] = exp;
				numDb++;
			}
		}

		if (numDb!=2) {
			String msg = "hpcviewer currently does not support merging more than two databases";
			MessageDialog.openError(shell, "Unsupported action", msg);
			return;
		}
		
		final RootScopeType mergeType;
		if (param.equals(PARAM_VALUE_TOPDOWN)) {
			mergeType = RootScopeType.CallingContextTree;
			
		} else if (param.equals(PARAM_VALUE_FLAT)) {
			mergeType = RootScopeType.Flat;
			
		} else {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Error: merge param unknown: " + param);

			return;
		}
		
		final Display display = shell.getDisplay();
		display.asyncExec(new Runnable() {
			
			@Override
			public void run() {
				try {
					Experiment mergedExp = ExperimentMerger.merge(db[0], db[1], mergeType);
					database.createViewsAndAddDatabase(mergedExp, application, service, modelService, null);
					
				} catch (Exception e) {
					MessageDialog.openError(shell, "Error merging database",
							e.getClass().getName() + ": \n" + e.getMessage());
				}
				
			}
		});
	}
	
	
	@CanExecute
	public boolean canExecute( @Optional @Named(PARAM_ID) String param) {
		
		// temporarily, we don't support merging more than 2 databases
		
		Iterator<BaseExperiment> iterator = database.getIterator();
		
		int numDb = 0;
		
		while(iterator.hasNext()) {
			Experiment exp = (Experiment) iterator.next();
			if (exp.isMergedDatabase()) {
				
				// if we already merge the topdowns, we should disable topdown merge
				// similarly, if the merged flat exists, we disable the flat merge
				
				Object []roots = exp.getRootScopeChildren();
				if (roots == null) continue;
				
				RootScope root = (RootScope) roots[0];
				
				if (root.getType()== RootScopeType.CallingContextTree && param.equals(PARAM_VALUE_TOPDOWN)) {
					return false;
				}
				if (root.getType()== RootScopeType.Flat && param.equals(PARAM_VALUE_FLAT)) {
					return false;
				}
			} else {
				numDb++;
			}
		}
		
		return numDb==2;
	}
		
}