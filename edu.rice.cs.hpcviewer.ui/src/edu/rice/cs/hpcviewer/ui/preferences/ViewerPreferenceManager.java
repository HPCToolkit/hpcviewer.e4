package edu.rice.cs.hpcviewer.ui.preferences;

import java.io.File;
import java.net.URL;

import javax.inject.Singleton;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.osgi.service.datalocation.Location;


@Creatable
@Singleton
public class ViewerPreferenceManager 
{
	public final static String  PREF_FILENAME       = "hpcviewer.prefs";

	public final static ViewerPreferenceManager INSTANCE = new ViewerPreferenceManager();
	
	private PreferenceStore preferenceStore;
	

	static public IEclipsePreferences getPreference() {
		return InstanceScope.INSTANCE.getNode(PreferenceConstants.P_HPCVIEWER);
	}
	
	public PreferenceStore getPreferenceStore() {
		
		if (preferenceStore == null) {
			
			Location location = Platform.getInstanceLocation();
			try {
				String directory = location.getURL().getFile();
				URL url = new URL("file", null, directory + "/" + PREF_FILENAME);
				String path = url.getFile();
				
				preferenceStore = new PreferenceStore(path);
				
				File file = new File(path);
				if (file.canRead())
					preferenceStore.load();
				
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		return preferenceStore;
	}

	
	/**
	 * Get the last path of the opened directory
	 * @return the last recorded path
	 */
	static public String getLastPath() {

		IEclipsePreferences prefViewer = getPreference();
		
		if(prefViewer != null) {
			return prefViewer.get(PreferenceConstants.P_PATH, ".");
		}
		return null;
	}
	
	
	/***
	 * Add the last used path into registry
	 * @param path
	 */
	static public void setLastPath(String path) {

		IEclipsePreferences prefViewer = getPreference();
		if (prefViewer != null) {
			prefViewer.put(PreferenceConstants.P_PATH, path);
		}
	}	
}
