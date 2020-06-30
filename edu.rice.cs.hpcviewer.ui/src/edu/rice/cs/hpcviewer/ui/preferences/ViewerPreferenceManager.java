package edu.rice.cs.hpcviewer.ui.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Shell;


public class ViewerPreferenceManager 
{

	private final PreferenceManager mgr;
	private final PropertiesResources resources;

	/**
	 * Get the last path of the opened directory
	 * @return the last recorded path
	 */
	static public String getLastPath() {

		IEclipsePreferences prefViewer = InstanceScope.INSTANCE.getNode(PreferenceConstants.P_HPCVIEWER);
		
		if(prefViewer != null) {
			return prefViewer.get(PreferenceConstants.P_PATH, ".");
		}
		return null;
	}
	
	static public void setLastPath(String path) {

		IEclipsePreferences prefViewer = InstanceScope.INSTANCE.getNode(PreferenceConstants.P_HPCVIEWER);
		if (prefViewer != null) {
			prefViewer.put(PreferenceConstants.P_PATH, path);
		}
	}
	
	public ViewerPreferenceManager() {
		
		mgr = new PreferenceManager();
		resources = new PropertiesResources();
		
		PreferenceNode profile = new PreferenceNode("Profile");
		profile.setPage(new MainProfilePage(resources, "hpcviewer profile"));
		mgr.addToRoot(profile);
		
	}
	
	
	public void run(Shell shell) {
		PreferenceDialog dlg = new PreferenceDialog(shell, mgr);
		
		dlg.open();
	}	
}
