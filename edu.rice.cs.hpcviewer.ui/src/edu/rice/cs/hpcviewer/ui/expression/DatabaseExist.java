// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcviewer.ui.expression;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Evaluate;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;

import edu.rice.cs.hpcbase.ui.IProfilePart;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public class DatabaseExist 
{
	@Inject DatabaseCollection database;
	
	@Evaluate
	public boolean evaluate(MWindow window, @Optional @Named(IServiceConstants.ACTIVE_PART) MPart activePart) {
		
		if (activePart == null) {
			// if activePart is null, it means we are in trouble: there's something wrong with Eclipse
			// and we don't know why. 
			return false;
		}
		
		if (database.getNumDatabase(window)>0) {
			return (activePart.getObject() instanceof IProfilePart);
		}
		return false;
	}
}
