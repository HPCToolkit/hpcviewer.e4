package edu.rice.cs.hpcsetting.preferences;

import java.io.File;
import java.io.FileNotFoundException;
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
			
			String filename = getPreferenceStoreLocation();
			try {
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

			} catch (FileNotFoundException e) {
				File file = new File(filename);
				try {
					if (!file.createNewFile())
						return null;
				} catch (IOException e1) {
					// not accessible error
					// nothing we can do
				}

			} catch (MalformedURLException e) {
				// this can't be right
				var logger = LoggerFactory.getLogger(getClass());
				logger.error("MalformedURLException: " + e.getMessage());
			} catch (IOException e) {
				var logger = LoggerFactory.getLogger(getClass());
				logger.error("Something wrong with the IO:" + e.getMessage());
			}
		}
		return preferenceStore;
	}
	
	
	public String getPreferenceStoreLocation() {
		Location location = Platform.getInstanceLocation();
		
		String directory = location.getURL().getFile();
		return directory + PreferenceConstants.PREF_FILENAME;
	}

	public abstract void setDefaults();

}
