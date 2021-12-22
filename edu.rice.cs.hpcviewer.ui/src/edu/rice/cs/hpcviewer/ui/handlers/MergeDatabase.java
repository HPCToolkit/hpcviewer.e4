 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.merge.ExperimentMerger;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcmerge.DatabaseMergeWizard;
import edu.rice.cs.hpcmerge.DatabasesToMerge;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
		List<Experiment> db = new ArrayList<Experiment>(2);
		
		Iterator<BaseExperiment> iterator = database.getIterator(application.getSelectedElement());
		while(iterator.hasNext()) {
			Experiment exp = (Experiment) iterator.next();
			if (!exp.isMergedDatabase()) {
				db.add(exp);
			}
		}

		if (db.size() < 2) {
			throw new RuntimeException("Can't merge one database");
		} if (db.size() > 2) {
			DatabaseMergeWizard dmw = new DatabaseMergeWizard(db);
			WizardDialog dialog = new WizardDialog(shell, dmw);
			
			if (dialog.open() == Dialog.CANCEL)
				return;
			
			DatabasesToMerge dm = dmw.getDatabaseToMerge();
			db.set(0, dm.experiment[0]);
			db.set(1, dm.experiment[1]);
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
		// dummy final variables so that the java thread can access to db[] variables
		// TODO fix this
		final Experiment e1 = db.get(0);
		final Experiment e2 = db.get(1);
		
		BusyIndicator.showWhile(shell.getDisplay(), () -> {
			try {
				Experiment mergedExp = ExperimentMerger.merge(e1, e2, mergeType);
				database.createViewsAndAddDatabase(mergedExp, application, service, modelService);
				
			} catch (Exception e) {
				MessageDialog.openError(shell, "Error merging database",
						e.getClass().getName() + ": \n" + e.getMessage());
			}
		});
	}
	
	
	@CanExecute
	public boolean canExecute(MApplication application, @Optional @Named(PARAM_ID) String param) {
		Iterator<BaseExperiment> iterator = database.getIterator(application.getSelectedElement());
		
		int numDb = 0;		
		while(iterator.hasNext()) {
			Experiment exp = (Experiment) iterator.next();
			if (!exp.isMergedDatabase()) {
				numDb++;
			}
		}
		
		return numDb>=2;
	}
		
}