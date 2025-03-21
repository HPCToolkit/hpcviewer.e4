// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcfilter.action;

import java.util.Map;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcfilter.cct.FilterPropertyDialog;


public class ShowFilterView 
{
	public static final String ID = "edu.rice.cs.hpcfilter.action.ShowFilterView";
	

	@Execute
	public void execute( @Named(IServiceConstants.ACTIVE_SHELL) Shell shell, 
							IEventBroker eventBroker,
							MApplication application) 
	{
		Map<String, Object> map = application.getTransientData();
		Object obj = map.get(ID);
		
		// if filter panel is already shown, even in other window, we should
		// bring it to the top.
		// we only allow one filter per application (or session)
		
		if (obj instanceof FilterPropertyDialog) {
			FilterPropertyDialog dlg = (FilterPropertyDialog) obj;
			if (dlg.getShell() != null && !dlg.getShell().isDisposed()) {
				dlg.getShell().setActive();
				return;
			}
		}
		FilterPropertyDialog dialog = new FilterPropertyDialog(shell, eventBroker);
		map.put(ID, dialog);
		
		dialog.open();
	}
}
