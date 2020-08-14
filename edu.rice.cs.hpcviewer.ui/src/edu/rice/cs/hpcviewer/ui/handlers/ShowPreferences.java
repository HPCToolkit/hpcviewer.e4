 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcsetting.preferences.AppearencePage;
import edu.rice.cs.hpcsetting.preferences.MainProfilePage;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceDialog;
import edu.rice.cs.hpctraceviewer.ui.preferences.TracePreferencePage;


public class ShowPreferences 
{
	@Execute
	public void execute(@Active Shell shell) {

		ViewerPreferenceDialog vprefDialog = new ViewerPreferenceDialog(shell);
		
		final String profileId    = "General";		
		final String appearenceId = "Appearence";

		vprefDialog.addPage(profileId, new MainProfilePage(profileId));
		vprefDialog.addPage(profileId, appearenceId, new AppearencePage(appearenceId));
		
		final String traceId = "Traces";
		
		vprefDialog.addPage(traceId, new TracePreferencePage(traceId));

		try {
			vprefDialog.open();
		} catch (Exception e) {
			e.printStackTrace();
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Error in the preference dialog", e);
		}
	}
		
}