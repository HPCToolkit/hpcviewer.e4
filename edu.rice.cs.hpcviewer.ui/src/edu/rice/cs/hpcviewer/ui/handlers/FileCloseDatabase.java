package edu.rice.cs.hpcviewer.ui.handlers;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public class FileCloseDatabase 
{
	@Inject DatabaseCollection database;

	private static final String ID_MENU_URI = "bundleclass://edu.rice.cs.hpcviewer.ui/" + 
												FileCloseDatabase.class.getName();
	
	private static final String ID_DATA_EXP = "viewer/data";

	@AboutToShow
	public void aboutToShow( List<MMenuElement> items, 
			DatabaseCollection database, 
			EModelService modelService, 
			MWindow window ) {

		Iterator<BaseExperiment> iterator = database.getIterator(window);

		while(iterator.hasNext()) {
			Experiment exp = (Experiment) iterator.next();

			String path    = exp.getDefaultDirectory().getAbsolutePath();
			String label   = path;

			if (exp.isMergedDatabase()) {
				label = "[Merged] " + label;
			}
			MDirectMenuItem menu = modelService.createModelElement(MDirectMenuItem.class);

			menu.setElementId(path);
			menu.setLabel(label);
			menu.setContributionURI(ID_MENU_URI);
			menu.getTransientData().put(ID_DATA_EXP, exp);

			// never ever set object or setContributorURI to the menu class
			// Eclipse will not be able to find URI contribution if the  object is set
			//
			//menu.setContributorURI("platform:/edu.rice.cs.hpcviewer.ui");
			//menu.setObject(exp);

			items.add(menu);
		}
	}


	@Execute
	public void execute(MDirectMenuItem menu) {

		if (database == null || database.isEmpty())
			return;

		if (menu == null)
			return;

		BaseExperiment exp = (BaseExperiment) menu.getTransientData().get(ID_DATA_EXP);
		database.removeDatabase(exp);
	}
}
