// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.util.ProcedureClassMap;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.dialog.ProcedureClassDialog;
import edu.rice.cs.hpctraceviewer.ui.internal.TraceEventData;
import edu.rice.cs.hpctraceviewer.ui.util.IConstants;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;

public class ProcedureColorMap 
{
	@Execute
	public void execute( @Named(IServiceConstants.ACTIVE_PART)  MPart part,
						 @Named(IServiceConstants.ACTIVE_SHELL) Shell shell, 
						 IEventBroker eventBroker) {
		
		ITracePart tracePart = (ITracePart) part.getObject();
		
		ProcedureClassMap classMap = new ProcedureClassMap(shell.getDisplay());
		ProcedureClassDialog dlg = new ProcedureClassDialog(shell, classMap);
		
		if ( dlg.open() == Dialog.OK ) {
			classMap.save();
			
			SpaceTimeDataController data = tracePart.getDataController();
			
			TraceEventData eventData = new TraceEventData(data, tracePart, data);
			eventBroker.post(IConstants.TOPIC_COLOR_MAPPING, eventData);
		}
	}
	
	
	@CanExecute
	public boolean canExecute() {
		
		return true;
	}
		
}