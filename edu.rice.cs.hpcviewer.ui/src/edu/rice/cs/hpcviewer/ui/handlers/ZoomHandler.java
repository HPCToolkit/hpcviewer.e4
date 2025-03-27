// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import edu.rice.cs.hpcbase.ui.IMainPart;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;

public class ZoomHandler 
{
	private static final String ParameterID = "edu.rice.cs.hpcviewer.ui.commandparameter.zoom";
	
	@Execute
	public void execute(MWindow window,
            EPartService partService,
            EModelService modelService,
            MPart part,
            @Named(ParameterID) String zoomType) {

		if (part == null)
			return;

		Object obj = part.getObject();
		if (!(obj instanceof IMainPart))
			return;
	}
	
	
	@CanExecute
	public boolean canExecute() {
		
		return true;
	}
		
}
