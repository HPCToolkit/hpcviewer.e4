package edu.rice.cs.hpcsetting.preferences;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.osgi.service.datalocation.Location;


public abstract class AbstractPreferenceManager 
{

	public final static String PREF_FILENAME = "hpcviewer.prefs";
	public static final String P_HPCVIEWER   = "edu.rice.cs.hpcsetting";
	
	private PreferenceStore preferenceStore;
	
	
	/****
	 * Retrieve the default preference based on {@link IEclipsePreferences}
	 * @return IEclipsePreferences
	 */
	static public IEclipsePreferences getPreference() {
		return InstanceScope.INSTANCE.getNode(P_HPCVIEWER);
	}
	
	
	/***
	 * Retrieve a {@link IPreferenceStore} object
	 * 
	 * @return PreferenceStore
	 */
	public PreferenceStore getPreferenceStore() {
		
		if (preferenceStore == null) {
			
			String filename = getPreferenceStoreLocation();
			try {
				URL url = new URL("file", null, filename);
				String path = url.getFile();
				
				preferenceStore = new PreferenceStore(path);
				
				setDefaults();
				
				// It is highly important to load the preference store as early as possible
				// before we use it to get the preference values
				// If the store is not loaded, we'll end up to get the default value all the time

				File file = new File(path);
				if (file.canRead())
					preferenceStore.load();
				
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		return preferenceStore;
	}
	
	
	public String getPreferenceStoreLocation() {
		Location location = Platform.getInstanceLocation();
		
		String directory = location.getURL().getFile();
		return directory + IPath.SEPARATOR + PREF_FILENAME;
	}

	abstract public void setDefaults();

}
