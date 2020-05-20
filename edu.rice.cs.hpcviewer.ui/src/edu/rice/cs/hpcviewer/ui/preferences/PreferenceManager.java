package edu.rice.cs.hpcviewer.ui.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;


public class PreferenceManager 
{

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
}
