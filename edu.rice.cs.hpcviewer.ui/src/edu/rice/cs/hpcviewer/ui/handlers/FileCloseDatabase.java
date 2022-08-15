package edu.rice.cs.hpcviewer.ui.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public class FileCloseDatabase extends DatabaseShowMenu
{
	@Inject DatabaseCollection database;

	private static final String ID_MENU_URI = "bundleclass://edu.rice.cs.hpcviewer.ui/" + 
												FileCloseDatabase.class.getName();

	@Execute
	public void execute(MApplication application, 
						MDirectMenuItem menu, 
						EModelService modelService, 
						EPartService partService) {

		if (database == null || database.isEmpty(application.getSelectedElement()))
			return;

		if (menu == null)
			return;

		BaseExperiment exp = (BaseExperiment) menu.getTransientData().get(ID_DATA_EXP);
		database.removeDatabase(application, modelService, partService, exp);
	}


	@Override
	protected DatabaseCollection getDatabase() {
		return database;
	}


	@Override
	protected String getMenuURI() {
		return ID_MENU_URI;
	}
}
