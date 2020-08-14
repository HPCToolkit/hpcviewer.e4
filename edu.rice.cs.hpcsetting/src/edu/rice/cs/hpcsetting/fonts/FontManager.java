package edu.rice.cs.hpcsetting.fonts;

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

import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;


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

	public Font getPreferenceFont(String id) {
		ViewerPreferenceManager prefManager = ViewerPreferenceManager.INSTANCE;
		PreferenceStore preferenceStore = prefManager.getPreferenceStore();
		
		FontData []data = PreferenceConverter.getFontDataArray(preferenceStore, id);
		
		return resource.createFont(FontDescriptor.createFrom(data));
	}

	
	/***
	 * get the font for generic text
	 * @return
	 */
	static public Font getFontGeneric() {
		return INSTANCE.getPreferenceFont(PreferenceConstants.ID_FONT_GENERIC);
	}
	
	
	/***
	 * get the fixed font for metrics
	 * @return
	 */
	static public Font getMetricFont() {
		return INSTANCE.getPreferenceFont(PreferenceConstants.ID_FONT_METRIC);
	}	
	
	
	/***
	 * get the fixed font for metrics
	 * @return
	 */
	static public Font getTextEditorFont() {
		return INSTANCE.getPreferenceFont(PreferenceConstants.ID_FONT_TEXT);
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
    
	
	/*****
	 * Set and save the font preference
	 * @see PreferenceConstants.ID_FONT_GENERIC
	 * @see PreferenceConstants.ID_FONT_METRICT
	 *   
	 * @param fontPreferenceID the preference ID
	 * @param fontData the new font data
	 * @throws IOException
	 */
	static public void setFontPreference(String fontPreferenceID, FontData[] fontData) throws IOException {

		PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		
		PreferenceConverter.setValue(pref, fontPreferenceID, fontData);
		pref.save();
	}
}
