 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import edu.rice.cs.hpcbase.IDatabase;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpclocal.DatabaseLocal;
import edu.rice.cs.hpcmerge.MergeManager;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.CanExecute;

public class MergeDatabase 
{
	private static final String PARAM_ID = "edu.rice.cs.hpcviewer.ui.commandparameter.merge";
	
	private static final String PARAM_VALUE_TOPDOWN = "topdown";
	private static final String PARAM_VALUE_FLAT    = "flat";
	
	@Inject DatabaseCollection database;
	
	@Execute
	public void execute(@Active Shell shell, @Optional @Named(PARAM_ID) String param,
			MWindow window,
			EPartService service,
			IEventBroker broker,
			EModelService modelService) {
		
		// ---------------------------------------------------------------
		// gather 2 databases to be merged.
		// we don't want to merge an already merged database. Skip it.
		// ---------------------------------------------------------------
		List<Experiment> listDatabase = new ArrayList<>(2);
		
		var iterator = database.getIterator(window);
		while(iterator.hasNext()) {
			var db = iterator.next();
			Experiment exp = (Experiment) db.getExperimentObject();
			if (!exp.isMergedDatabase()) {
				listDatabase.add(exp);
			}
		}

		if (listDatabase.size() < 2) {
			throw new IllegalArgumentException("Can't merge one database");
		}
		
		// check the type of merging: top-down or flat. 
		// bottom up is not supported yet
		RootScopeType type;
		
		if (param.equals(PARAM_VALUE_TOPDOWN)) {
			type = RootScopeType.CallingContextTree;			
		} else if (param.equals(PARAM_VALUE_FLAT)) {
			type = RootScopeType.Flat;			
		} else {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Error: merge param unknown: " + param);
			return;
		}
		
		// everything looks fine: do the merging
		MergeManager.merge(shell, listDatabase, type, new MergeManager.IMergeCallback() {			
			@Override
			public void mergeError(String errorMsg) {
				MessageDialog.openError(shell, "Error merging database", errorMsg);
			}
			
			@Override
			public void mergeDone(Experiment experiment) {
				var db = new DatabaseMerged(experiment);
				database.addDatabase(shell, window, service, modelService, db);
			}
		});
	}
	
	
	@CanExecute
	public boolean canExecute(MApplication application, @Optional @Named(PARAM_ID) String param) {
		var iterator = database.getIterator(application.getSelectedElement());
		
		int numDb = 0;		
		while(iterator.hasNext()) {
			var db = iterator.next();
			Experiment exp = (Experiment) db.getExperimentObject();
			if (!exp.isMergedDatabase()) {
				numDb++;
			}
		}		
		return numDb>=2;
	}
	
	
	static class DatabaseMerged implements IDatabase
	{
		final Experiment experiment;
		
		public DatabaseMerged(Experiment experiment) {
			this.experiment = experiment;
		}

		@Override
		public String getId() {
			return experiment.getID();
		}

		@Override
		public DatabaseStatus open(Shell shell) {
			return DatabaseStatus.OK;
		}

		@Override
		public DatabaseStatus getStatus() {
			return DatabaseStatus.OK;
		}

		@Override
		public void close() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public IExperiment getExperimentObject() {
			return experiment;
		}
		
	}
		
}