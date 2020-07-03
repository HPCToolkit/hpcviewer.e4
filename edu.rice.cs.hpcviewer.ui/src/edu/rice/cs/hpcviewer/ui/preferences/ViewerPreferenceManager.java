package edu.rice.cs.hpcviewer.ui.preferences;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import javax.inject.Singleton;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.service.datalocation.Location;


@Creatable
@Singleton
/**************************************
 * 
 * The center of hpcviewer preferences.
 * This class manages preferences load, set and location.
 * There are two type of supported preference storage:
 * <ul>
 * <li>IEclipsePreference for hierarchical storage
 * <li>IPreferenceStore to store fonts and primitive types
 * </ul>
 * 
 **************************************/
public class ViewerPreferenceManager 
{
	public final static String  PREF_FILENAME = "hpcviewer.prefs";

	public final static ViewerPreferenceManager INSTANCE = new ViewerPreferenceManager();
	
	private PreferenceStore preferenceStore;

	private HashMap<String, Object> defaultPreferences;
	
	/****
	 * Retrieve the default preference based on {@link IEclipsePreferences}
	 * @return IEclipsePreferences
	 */
	static public IEclipsePreferences getPreference() {
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

	
	/****
	 * get the default preference of a give preference Id.
	 * @param id see {@link PreferenceConstants} class
	 * @return Object 
	 */
	public Object getDefault(String id) {
		initDefaults();
		
		return defaultPreferences.get(id);
	}
	
	
	/****
	 * Initialize the default preferences.
	 */
	private void initDefaults() {
		if (defaultPreferences != null)
			return;
		
		defaultPreferences = new HashMap<String, Object>();
		
		defaultPreferences.put(PreferenceConstants.ID_DEBUG_MODE,    Boolean.FALSE);
		defaultPreferences.put(PreferenceConstants.ID_DEBUG_CCT_ID,  Boolean.FALSE);
		defaultPreferences.put(PreferenceConstants.ID_DEBUG_FLAT_ID, Boolean.FALSE);
		
		defaultPreferences.put(PreferenceConstants.ID_FONT_GENERIC, 
							   	JFaceResources.getDefaultFont());
		defaultPreferences.put(PreferenceConstants.ID_FONT_METRIC, 
				   				JFaceResources.getTextFont());
		defaultPreferences.put(PreferenceConstants.ID_FONT_TEXT, 
   								JFaceResources.getTextFont());
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
	
	
	public boolean getDebugMode() {
		if (preferenceStore == null)
			return false;
		
		return preferenceStore.getBoolean(PreferenceConstants.ID_DEBUG_MODE);
	}
	
	public boolean getDebugCCT() {
		return getDebugStatus(PreferenceConstants.ID_DEBUG_CCT_ID);
	}
	
	
	public boolean getDebugFlat() {
		return getDebugStatus(PreferenceConstants.ID_DEBUG_FLAT_ID);
	}
	
	
	private boolean getDebugStatus(String id) {
		if (preferenceStore == null)
			return false;
		
		boolean debug = preferenceStore.getBoolean(PreferenceConstants.ID_DEBUG_MODE);
		boolean debugSub = preferenceStore.getBoolean(id);
		
		return debug && debugSub;
	}
}