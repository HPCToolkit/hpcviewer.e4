// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;
import edu.rice.cs.hpctraceviewer.ui.TracePart;

import org.eclipse.e4.core.di.annotations.CanExecute;

public class ShowDebugView 
{
	@Execute
	public void execute(MPart part) {
		Object obj = part.getObject();
		if (!(obj instanceof TracePart))
			return;

		TracePart tracePart = (TracePart) obj;
		tracePart.createDebugView();
	}
	
	
	@CanExecute
	public boolean canExecute(MPart part) {
		Object obj = part.getObject();
		if (!(obj instanceof TracePart))
			return false;

		TracePart tracePart = (TracePart) obj;
		return ViewerPreferenceManager.INSTANCE.getDebugMode() && !tracePart.isDebugViewShown();
	}		
}