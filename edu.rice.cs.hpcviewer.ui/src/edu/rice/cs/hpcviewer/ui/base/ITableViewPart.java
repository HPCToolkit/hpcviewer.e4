// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcviewer.ui.base;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import edu.rice.cs.hpcbase.ui.ILowerPart;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;


public interface ITableViewPart extends ILowerPart 
{
	void setService(EPartService partService, 
						   IEventBroker broker,
						   DatabaseCollection database,
						   ProfilePart   profilePart,
						   EMenuService  menuService);

}
