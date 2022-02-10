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
	public static final ViewerPreferenceManager INSTANCE = new ViewerPreferenceManager();
	public static final String DEFAULT_CALLTO[]   = new String[] {"\u21DB", "\u21D2", "\u21D8", "\u27A5", "\u27F1", "\u2937", "\u2B0A", "\u2B0E", "\u2B46", "\u2B78", "\u2BA1", "\u2BA9", "\u2BB1"};
	public static final String DEFAULT_CALLFROM[] = new String[] {"\u21DA", "\u21D0", "\u21D6", "\u2BAA", "\u27F0", "\u293A", "\u2B09", "\u2B11", "\u2B45", "\u2B76", "\u2BA2", "\u2BAA", "\u2BB2"};
	
 	private static final String EMPTY = "";
		
	
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
		
		store.setDefault(PreferenceConstants.ID_CHAR_CALLTO, "\u21DB");
		store.setDefault(PreferenceConstants.ID_CHAR_CALLFROM, "\u21DA");
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
	
	public String getCallToCharacter() {
		var store = getPreferenceStore();
		if (store == null)
			return EMPTY;
		return store.getString(PreferenceConstants.ID_CHAR_CALLTO);
	}
	
	
	public String getCallFromCharacter() {
		var store = getPreferenceStore();
		if (store == null)
			return EMPTY;
		return store.getString(PreferenceConstants.ID_CHAR_CALLFROM);
	}
}