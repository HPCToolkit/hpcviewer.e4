package edu.rice.cs.hpcsetting.preferences;

import java.io.IOException;


import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.JFaceResources;



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
public class ViewerPreferenceManager extends AbstractPreferenceManager
{
	public final static ViewerPreferenceManager INSTANCE = new ViewerPreferenceManager();
	
		
	
	/****
	 * Initialize the default preferences.
	 * @throws IOException 
	 */
	@Override
	public void setDefaults() {
		
		PreferenceStore store = getPreferenceStore();
		store.setDefault(PreferenceConstants.ID_DEBUG_MODE,    Boolean.FALSE);
		store.setDefault(PreferenceConstants.ID_DEBUG_CCT_ID,  Boolean.FALSE);
		store.setDefault(PreferenceConstants.ID_DEBUG_FLAT_ID, Boolean.FALSE);
		
		PreferenceConverter.setDefault(getPreferenceStore(), 
				PreferenceConstants.ID_FONT_GENERIC, JFaceResources.getDefaultFont().getFontData());
		PreferenceConverter.setDefault(getPreferenceStore(), 
				PreferenceConstants.ID_FONT_METRIC, JFaceResources.getTextFont().getFontData());
		PreferenceConverter.setDefault(getPreferenceStore(), 
				PreferenceConstants.ID_FONT_TEXT, JFaceResources.getTextFont().getFontData());
		
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
		if (getPreferenceStore() == null)
			return false;
		
		return getPreferenceStore().getBoolean(PreferenceConstants.ID_DEBUG_MODE);
	}
	
	public boolean getDebugCCT() {
		return getDebugStatus(PreferenceConstants.ID_DEBUG_CCT_ID);
	}
	
	
	public boolean getDebugFlat() {
		return getDebugStatus(PreferenceConstants.ID_DEBUG_FLAT_ID);
	}
	
	
	private boolean getDebugStatus(String id) {
		if (getPreferenceStore() == null)
			return false;
		
		boolean debug = getPreferenceStore().getBoolean(PreferenceConstants.ID_DEBUG_MODE);
		boolean debugSub = getPreferenceStore().getBoolean(id);
		
		return debug && debugSub;
	}
}