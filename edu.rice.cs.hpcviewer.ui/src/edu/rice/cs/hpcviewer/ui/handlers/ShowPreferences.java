 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcviewer.ui.preferences.MainProfilePage;
import edu.rice.cs.hpcviewer.ui.preferences.PropertiesResources;

public class ShowPreferences 
{
	@Execute
	public void execute(@Active Shell shell) {
		
		PreferenceManager mgr = new PreferenceManager();
		PropertiesResources resources = new PropertiesResources();
	
		PreferenceNode profile = new PreferenceNode("Profile");
		profile.setPage(new MainProfilePage(resources, "hpcviewer profile"));
		mgr.addToRoot(profile);

		PreferenceDialog dlg = new PreferenceDialog(shell, mgr);
		
		dlg.open();
	}
		
}