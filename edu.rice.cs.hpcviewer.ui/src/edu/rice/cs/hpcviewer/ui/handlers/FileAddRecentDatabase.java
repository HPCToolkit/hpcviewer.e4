package edu.rice.cs.hpcviewer.ui.handlers;

import javax.inject.Inject;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public class FileAddRecentDatabase extends RecentDatabase 
{
	private static final String ID_MENU_URI = "bundleclass://edu.rice.cs.hpcviewer.ui/" + 
											   FileAddRecentDatabase.class.getName();

	@Inject DatabaseCollection dbCollection;
	
	@Override
	protected void execute(MApplication application, 
						   EModelService modelService, 
						   EPartService partService, 
						   Shell shell,
						   String database) {
		
		dbCollection.switchDatabase(shell, application, partService, modelService, database);
	}

	@Override
	protected String getURI() {
		return ID_MENU_URI;
	}

}
