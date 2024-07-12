// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcviewer.ui.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public class FileSwitchDatabase 
{	
	@Inject DatabaseCollection databaseCollection;
	
	@Execute
	public void execute(MWindow window,
						EPartService partService, 
						EModelService modelService,
						@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		
		databaseCollection.switchDatabase(shell, window, partService, modelService, null);
	}

}
