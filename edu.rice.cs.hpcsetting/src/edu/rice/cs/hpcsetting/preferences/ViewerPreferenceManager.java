package edu.rice.cs.hpcsetting.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.JFaceResources;

import edu.rice.cs.hpcsetting.fonts.FontManager;



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
	
	// The symbols in DEFAULT_CALLTO and DEFAULT_CALLFROM have to match
	// and they should have the same number of items
	public static final String []DEFAULT_CALLTO   = new String[] {"\u00bb", "\u21C9", "\u21D2", "\u21D8", "\u21DB", "\u21E5", "\u21F2", "\u27A5", "\u2937", "\u2B0A", "\u2B0E", "\u2B46", "\u2B78", "\u2BA1", "\u2BA9", "\u2BAF", "\u2BB1"};
	public static final String []DEFAULT_CALLFROM = new String[] {"\u00ab", "\u21C7", "\u21D0", "\u21D6", "\u21DA", "\u21E4", "\u21F1", "\u27A6", "\u293A", "\u2B09", "\u2B11", "\u2B45", "\u2B76", "\u2BA2", "\u2BAA", "\u2BAC", "\u2BB2"};

	// fix issue #207 (some platforms don't support unicode)
 	public static final int DEFAULT_CALLSITE_INDEX = 0;

 	private static final String EMPTY = "";
	
	/****
	 * Initialize the default preferences.
	 * <ul>
	 *  <li>By default, all debug options are false (disabled).
	 *  <li>The default fonts depend on the JFaceResources 
	 * 		 as we don't bother to look all the available fonts.
	 *  <li>The default call site symbols are hard coded. Anyone wants
	 *  		to add symbols have to modify this code.
	 * </ul>
	 */
	@Override
	public void setDefaults() {
		
		PreferenceStore store = getPreferenceStore();
		store.setDefault(PreferenceConstants.ID_DEBUG_MODE,    Boolean.FALSE);
		store.setDefault(PreferenceConstants.ID_DEBUG_CCT_ID,  Boolean.FALSE);
		store.setDefault(PreferenceConstants.ID_DEBUG_FLAT_ID, Boolean.FALSE);
		
		PreferenceConverter.setDefault(store, 
									   PreferenceConstants.ID_FONT_GENERIC, 
									   JFaceResources.getDefaultFont().getFontData());
		PreferenceConverter.setDefault(store, 
									   PreferenceConstants.ID_FONT_METRIC, 
									   JFaceResources.getTextFont().getFontData());
		PreferenceConverter.setDefault(store, 
									   PreferenceConstants.ID_FONT_TEXT, 
									   JFaceResources.getTextFont().getFontData());
		PreferenceConverter.setDefault(store, 
									   PreferenceConstants.ID_FONT_CALLSITE, 
									   FontManager.getCallsiteGlyphDefaultFont().getFontData());
		
		// fix issue #207 (some platforms don't support unicode)
		// Use ASCII character by default

		store.setDefault(PreferenceConstants.ID_CHAR_CALLTO, DEFAULT_CALLTO[DEFAULT_CALLSITE_INDEX]);
		store.setDefault(PreferenceConstants.ID_CHAR_CALLFROM, DEFAULT_CALLFROM[DEFAULT_CALLSITE_INDEX]);
	}
	
	
	/**
	 * Get the last path of the opened directory
	 * @return the last recorded path
	 */
	public static String getLastPath() {

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
	public static void setLastPath(String path) {

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
	
	public String getCallToGlyph() {
		var store = getPreferenceStore();
		if (store == null)
			return EMPTY;
		return store.getString(PreferenceConstants.ID_CHAR_CALLTO);
	}
	
	
	public String getCallFromGlyph() {
		var store = getPreferenceStore();
		if (store == null)
			return EMPTY;
		return store.getString(PreferenceConstants.ID_CHAR_CALLFROM);
	}
	
	
	public boolean getExperimentalFeatureMode() {
		var store = getPreferenceStore();
		if (store == null)
			return false;
		return store.getBoolean(PreferenceConstants.ID_FEATURE_EXPERIMENTAL);
	}
}
