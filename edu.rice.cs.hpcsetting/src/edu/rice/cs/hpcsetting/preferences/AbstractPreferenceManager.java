package edu.rice.cs.hpcsetting.preferences;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.osgi.service.datalocation.Location;
import org.slf4j.LoggerFactory;


public abstract class AbstractPreferenceManager 
{
	private PreferenceStore preferenceStore;
	
	
	/****
	 * Retrieve the default preference based on {@link IEclipsePreferences}
	 * @return IEclipsePreferences
	 */
	public static IEclipsePreferences getPreference() {
		return InstanceScope.INSTANCE.getNode(PreferenceConstants.P_HPCVIEWER);
	}
	
	
	/***
	 * Retrieve a {@link IPreferenceStore} object
	 * 
	 * @return PreferenceStore
	 */
	public PreferenceStore getPreferenceStore() {
		
		if (preferenceStore == null) {
			
			try {
				String filename = getPreferenceStoreLocation();
				URL url = new URL("file", null, filename);
				String path = url.getFile();
				
				preferenceStore = new PreferenceStore(path);
				
				// It is highly important to load the preference store as early as possible
				// before we use it to get the preference values
				// If the store is not loaded, we'll end up to get the default value all the time
				//
				// btw, I don't know why we need to set the default AFTER loading the preference
				// If we set the default BEFORE it, the loading doesn't work properly.
				
				File file = new File(path);
				if (file.canRead())
					preferenceStore.load();

				// has to set the default AFTER the loading
				setDefaults();


			} catch (MalformedURLException e) {
				// this can't be right
				var logger = LoggerFactory.getLogger(getClass());
				logger.error("MalformedURLException: " + e.getMessage());
			} catch (Exception e) {
				var logger = LoggerFactory.getLogger(getClass());
				logger.error("Something wrong with the IO:" + e.getMessage());
			}
		}
		if (preferenceStore == null) {
			try {
				var tmpFile = File.createTempFile("pref", "hpcviewer");
				preferenceStore = new PreferenceStore(tmpFile.getAbsolutePath());
			} catch (IOException e) {
				// gives up
			}
		}
		return preferenceStore;
	}
	

	/****
	 * get the file path of the preference
	 * @return String
	 */
	public String getPreferenceStoreLocation() {
		Location location = Platform.getInstanceLocation();
		
		String directory = location.getURL().getFile();
		return directory + PreferenceConstants.PREF_FILENAME;
	}

	public abstract void setDefaults();

}
