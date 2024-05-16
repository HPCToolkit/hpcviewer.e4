package edu.rice.cs.hpcviewer.ui.handlers;


import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcremote.ICollectionOfConnections;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public class FileExit 
{	
	@Execute
	public void execute(
			IWorkbench workbench, 
			@Optional DatabaseCollection databaseCollection,
			@Optional EModelService modelService,
			@Optional EPartService  partService) {
		
		var application = workbench.getApplication();
		if (application != null && application.getSelectedElement() != null) {
			var window = application.getSelectedElement();
			if (databaseCollection != null) {
				databaseCollection.removeAllDatabases(window, modelService, partService);
			}
			ICollectionOfConnections.disconnectAll((Shell) window.getWidget());
		}
		workbench.close();
	}
}
