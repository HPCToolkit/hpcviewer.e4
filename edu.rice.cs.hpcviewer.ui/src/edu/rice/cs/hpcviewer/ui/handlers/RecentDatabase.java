// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcviewer.ui.handlers;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcbase.IDatabaseIdentification;
import edu.rice.cs.hpcbase.map.UserInputHistory;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.internal.DatabaseFactory;

public abstract class RecentDatabase 
{
	public  static final String HISTORY_DATABASE_RECENT = "recent";
	public  static final int    HISTORY_MAX = 20;
	private static final String ID_DATA_ECP = "viewer/recent";
		
	
	@AboutToShow
	public void aboutToShow( List<MMenuElement> items, 
			DatabaseCollection database, 
			EModelService modelService, 
			MWindow window ) {
		
		UserInputHistory history = new UserInputHistory(HISTORY_DATABASE_RECENT, HISTORY_MAX);
		if (history.getHistory().isEmpty())
			return;
		
		int i = 0;
		for(String db: history.getHistory()) {
			if (i++ > HISTORY_MAX)
				break;
			
			MDirectMenuItem menu = modelService.createModelElement(MDirectMenuItem.class);
			
			var dbId = DatabaseFactory.createDatabaseIdentification(db);
			if (dbId == null)
				continue;
			
			var dbIdStr = dbId.toString();
			
			menu.setElementId(db);
			menu.setLabel(dbIdStr);
			menu.setContributionURI(getURI());
			menu.getTransientData().put(ID_DATA_ECP, db);
			
			items.add(menu);
		}
		addClearMenu(items, modelService);
	}

	private void addClearMenu(
			List<MMenuElement> items, 
			EModelService modelService) {
		
		var separator = modelService.createModelElement(MMenuSeparator.class);
		items.add(separator);
		
		MDirectMenuItem menu = modelService.createModelElement(MDirectMenuItem.class);
		
		final String label = "Clear history";
		
		menu.setElementId(label);
		menu.setLabel(label);
		menu.setContributionURI(getURI());
		menu.getTransientData().put(ID_DATA_ECP, null);
		
		items.add(menu);

	}

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell,
						MApplication application, 
						MWindow window,
						MDirectMenuItem menu, 
						EModelService modelService, 
						EPartService partService) {
	
		String db = (String) menu.getTransientData().get(ID_DATA_ECP);
		if (db == null) {
			UserInputHistory history = new UserInputHistory(HISTORY_DATABASE_RECENT, HISTORY_MAX);
			history.clear();
		} else {
			var dbId = DatabaseFactory.createDatabaseIdentification(db);
			
			// if the database is removed or moved to somewhere else, we have
			// to notify the user
			if (dbId == null) {
				MessageDialog.openError(
						shell, 
						"Error opening the database",
						db + ": Database is not accessible.");
			}
			
			execute(application, window, modelService, partService, shell, dbId);
		}
	}

	
	protected abstract String getURI();
	
	protected abstract void execute(MApplication application, 
								    MWindow      window,
									EModelService modelService, 
									EPartService partService, 
									Shell shell, 
									IDatabaseIdentification database);
}