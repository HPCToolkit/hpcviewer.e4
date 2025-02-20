// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcviewer.ui.handlers;

import javax.inject.Inject;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.util.IConstants;

public class NewWindow 
{
	static public final String ID_WINDOW = "edu.rice.cs.hpcviewer.window.main";
	static private final String ID_OPEN_COMMAND = "edu.rice.cs.hpcviewer.ui.command.open";
	
	@Inject DatabaseCollection database;
	
	@Execute
	public void createWindow(EModelService modelService, MApplication app) {
		MTrimmedWindow newWin = (MTrimmedWindow)modelService.cloneSnippet(app, "edu.rice.cs.hpcviewer.ui.trimmedwindow.main", null);

	    Rectangle rect = Display.getDefault().getPrimaryMonitor().getBounds();
	    int w = Math.min(IConstants.WINDOW_WIDTH,  rect.width);
		int h = Math.min(IConstants.WINDOW_HEIGHT, rect.height);
		newWin.setWidth(w);
		newWin.setHeight(h);
		
		app.getChildren().add(newWin);

		ECommandService cs = newWin.getContext().get(ECommandService.class);
		EHandlerService hs = newWin.getContext().get(EHandlerService.class);

		Command cmdOpen = cs.getCommand(ID_OPEN_COMMAND);
		ParameterizedCommand pc = new ParameterizedCommand(cmdOpen, null);

		hs.executeHandler(pc);
	}
}