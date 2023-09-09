 
package edu.rice.cs.hpcviewer.ui.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcbase.IDatabase;
import edu.rice.cs.hpcremote.data.RemoteDatabase;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public class OpenRemoteDatabase 
{
	@Inject EModelService modelService;
	
	@Inject DatabaseCollection databaseCollection;


	@Execute
	public void execute(
			IWorkbench workbench, 
			IEclipseContext context, 
			EPartService partService,
			MWindow window,
			@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		
		var remoteDb   = new RemoteDatabase();
		if (remoteDb.open(shell) != IDatabase.DatabaseStatus.OK)
			return;
		
		var experiment = remoteDb.getExperimentObject();
		
		if (experiment == null)
			return;
		
		databaseCollection.addDatabase(shell, window, partService, modelService, remoteDb);
	}		
}