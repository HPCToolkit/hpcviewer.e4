 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcviewer.ui.preferences.AppearencePage;
import edu.rice.cs.hpcviewer.ui.preferences.MainProfilePage;
import edu.rice.cs.hpcviewer.ui.preferences.PropertiesResources;

public class ShowPreferences 
{
	@Execute
	public void execute(@Active Shell shell) {
		
		PreferenceManager mgr = new PreferenceManager();
		PropertiesResources resources = new PropertiesResources();
	
		String profileId = "General";
		PreferenceNode profile = new PreferenceNode(profileId);
		profile.setPage(new MainProfilePage(resources, profileId));
		mgr.addToRoot(profile);
		
		String appearenceId = "Appearence";
		PreferenceNode appearence = new PreferenceNode(appearenceId);
		appearence.setPage(new AppearencePage(resources, appearenceId));		
		mgr.addTo(profileId, appearence);

		PreferenceDialog dlg = new PreferenceDialog(shell, mgr);
		dlg.create();
		dlg.getShell().setText("Preferences");
		dlg.getTreeViewer().expandAll();
		
		try {
			dlg.open();
		} catch (Exception e) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Error in the preference dialog", e);
		}
	}
		
}