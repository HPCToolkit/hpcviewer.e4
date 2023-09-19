package edu.rice.cs.hpcviewer.ui.handlers;

import javax.inject.Inject;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public class FileOpenRecentDatabase extends RecentDatabase 
{
	private static final String ID_MENU_URI = "bundleclass://edu.rice.cs.hpcviewer.ui/" + 
												FileOpenRecentDatabase.class.getName();

	@Inject DatabaseCollection databaseCollection;


	@Override
	protected void execute(	MApplication application, 
							MWindow      window,
							EModelService modelService, 
							EPartService partService, 
							Shell shell,
							String database) {
		
		databaseCollection.addDatabase(shell, window, partService, modelService, database);
	}


	@Override
	protected String getURI() {
		return ID_MENU_URI;
	}

}
