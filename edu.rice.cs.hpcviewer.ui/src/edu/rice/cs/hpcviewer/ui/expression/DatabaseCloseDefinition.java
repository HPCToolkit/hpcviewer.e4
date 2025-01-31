// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcviewer.ui.expression;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Evaluate;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public class DatabaseCloseDefinition 
{
	@Inject DatabaseCollection database;
	
	@Evaluate
	public boolean evaluate(MWindow window) {
		return database.getNumDatabase(window)>0;
	}
}
