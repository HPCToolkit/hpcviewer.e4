package edu.rice.cs.hpcviewer.ui.resources;

import java.io.IOException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import edu.rice.cs.hpcviewer.ui.preferences.PreferenceConstants;
import edu.rice.cs.hpcviewer.ui.preferences.ViewerPreferenceManager;


/*************************************************************
 * 
 * Class to manage fonts and its default fonts.
 * It allocates and de-allocate automatically fonts.
 * Need to access this class to get the prefered fonts.
 *
 *************************************************************/
public class FontManager 
{
	public final static FontManager INSTANCE = new FontManager();
	
	private final ResourceManager resource;
	
	public FontManager() {
		resource = new LocalResourceManager(JFaceResources.getResources());
	}

	public Font getPreferenceFont(String id, Font defaultFont) {
		ViewerPreferenceManager prefManager = ViewerPreferenceManager.INSTANCE;
		PreferenceStore preferenceStore = prefManager.getPreferenceStore();
		
		FontData []data = PreferenceConverter.getFontDataArray(preferenceStore, id);
		if (data == PreferenceConverter.getFontDataArrayDefaultDefault())
			return defaultFont;
		
		return resource.createFont(FontDescriptor.createFrom(data));
	}

	
	/***
	 * get the font for generic text
	 * @return
	 */
	static public Font getFontGeneric() {
		Font defaultFont = JFaceResources.getDefaultFont();
		return INSTANCE.getPreferenceFont(PreferenceConstants.ID_FONT_GENERIC, defaultFont);
	}
	
	
	/***
	 * get the fixed font for metrics
	 * @return
	 */
	static public Font getMetricFont() {
		Font defaultFont = JFaceResources.getTextFont();
		return INSTANCE.getPreferenceFont(PreferenceConstants.ID_FONT_METRIC, defaultFont);
	}	
	
	
	/***
	 * get the fixed font for metrics
	 * @return
	 */
	static public Font getTextEditorFont() {
		Font defaultFont = JFaceResources.getTextFont();
		return INSTANCE.getPreferenceFont(PreferenceConstants.ID_FONT_TEXT, defaultFont);
	}	
	

	/****
	 * Retrieve font data of a given font preference.
	 * @see PreferenceConstants.ID_FONT_GENERIC
	 * @see PreferenceConstants.ID_FONT_METRICT
	 *  
	 * @param fontPreferenceID The preference font id. 
	 * @return
	 */
	static public FontData[] getFontDataPreference(String fontPreferenceID) {

		IPreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		
		FontData []fd = PreferenceConverter.getFontDataArray(pref, fontPreferenceID);
		return fd;
	}
    
	
	static public void setFontPreference(String fontPreferenceID, FontData[] fontData) throws IOException {

		PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		
		PreferenceConverter.setValue(pref, fontPreferenceID, fontData);
		pref.save();
	}
}
