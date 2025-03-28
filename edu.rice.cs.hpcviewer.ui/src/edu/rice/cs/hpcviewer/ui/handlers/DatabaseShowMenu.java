// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcviewer.ui.handlers;

import java.util.List;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.hpctoolkit.db.local.experiment.Experiment;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public abstract class DatabaseShowMenu 
{	
	protected static final String ID_DATA_EXP = "viewer/data";
	
	@AboutToShow
	public void aboutToShow( List<MMenuElement> items, 
							 EModelService modelService, 
							 MWindow window ) {
		if (!canExecute(window))
			return;
		
		var iterator = getDatabase().getIterator(window);

		while(iterator.hasNext()) {
			var database = iterator.next();
			
			Experiment exp = (Experiment) database.getExperimentObject();
			String label = database.getId().toString();
			
			if (exp.isMergedDatabase()) {
				label = "[Merged] " + label;
			}
			MDirectMenuItem menu = modelService.createModelElement(MDirectMenuItem.class);
			
			menu.setElementId(label);
			menu.setLabel(label);
			menu.setContributionURI(getMenuURI());
			menu.getTransientData().put(ID_DATA_EXP, database);
			
			items.add(menu);
		}
	}

	
	@CanExecute
	public boolean canExecute(MWindow window) {		
		return getDatabase().getNumDatabase(window)>0;
	}

	protected abstract DatabaseCollection getDatabase();
	protected abstract String getMenuURI();
}
