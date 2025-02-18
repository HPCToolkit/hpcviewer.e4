// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcsetting.preferences.AppearencePage;
import edu.rice.cs.hpcsetting.preferences.DebugConfigPage;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceDialog;
import edu.rice.cs.hpctraceviewer.config.TracePreferencePage;


public class ShowPreferences 
{
	@Execute
	public void execute(@Active Shell shell) {

		ViewerPreferenceDialog vprefDialog = new ViewerPreferenceDialog(shell);
		
		vprefDialog.addPage(AppearencePage.TITLE, new AppearencePage());
		vprefDialog.addPage(TracePreferencePage.TITLE, new TracePreferencePage());
		vprefDialog.addPage(DebugConfigPage.TITLE, new DebugConfigPage());

		try {
			vprefDialog.open();
		} catch (Exception e) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Error in the preference dialog", e);
		}
	}
		
}