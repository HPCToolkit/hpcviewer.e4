package edu.rice.cs.hpcsetting.fonts;

import java.io.IOException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;


/*************************************************************
 * 
 * Class to manage fonts and its default fonts.
 * It allocates and de-allocate automatically fonts.
 * Need to access this class to get the preferred fonts.
 *
 *************************************************************/
public class FontManager 
{
	private static final String ID_FONT_CALLSITE_DEFAULT = "hpcviewer.font.def.cs";
	
	public final static FontManager INSTANCE = new FontManager();
	private final FontRegistry fontRegistry;
	
	public FontManager() {
		fontRegistry = new FontRegistry();
	}

	public Font getPreferenceFont(String id) {
		Font font = fontRegistry.get(id);
		if (font != fontRegistry.defaultFont())
			return font;
		
		ViewerPreferenceManager prefManager = ViewerPreferenceManager.INSTANCE;
		PreferenceStore preferenceStore = prefManager.getPreferenceStore();
		
		FontData []data = PreferenceConverter.getFontDataArray(preferenceStore, id);
		fontRegistry.put(id, data);
		return fontRegistry.get(id);
	}

	
	/***
	 * get the font for generic text
	 * @return
	 */
	static public Font getFontGeneric() {
		return getFont(PreferenceConstants.ID_FONT_GENERIC, JFaceResources.getDefaultFont());
	}
	
	
	/***
	 * get the fixed font for metrics
	 * @return
	 */
	static public Font getMetricFont() {
		return getFont(PreferenceConstants.ID_FONT_METRIC, JFaceResources.getTextFont());
	}	
	
	
	/***
	 * get the fixed font for metrics
	 * @return
	 */
	static public Font getTextEditorFont() {
		return getFont(PreferenceConstants.ID_FONT_TEXT, JFaceResources.getTextFont());
	}
	
	
	/******
	 * Retrieve the default font for the call-site symbol.
	 * It's basically the same as the generic font, but with taller font.
	 * 
	 * @return {@code Font}
	 */
	static public Font getCallsiteGlyphDefaultFont() {
		return getCallsiteGlyphDefaultFont(getFontGeneric().getFontData()[0]);
	}
	
	
	/****
	 * Get the default callsite glyph font based on a certain "base" font.
	 * The glyph font will have height higher than the base font.
	 * 
	 * @param fontBase
	 * 			The base font.
	 * @return
	 * 			{@code Font}
	 */
	static public Font getCallsiteGlyphDefaultFont(FontData fontBase) {
		int height = fontBase.getHeight();
		
		// we want the call glyph to be more visible
		// In most cases, having taller 2 pixels is enough
		final int ADJUSTED_SIZE = 2;
		
		FontDescriptor fd = FontDescriptor.createFrom(fontBase);
		var defaultFont   = fd.setHeight(ADJUSTED_SIZE + height);
		INSTANCE.fontRegistry.put(ID_FONT_CALLSITE_DEFAULT, defaultFont.getFontData());
		
		return INSTANCE.fontRegistry.get(ID_FONT_CALLSITE_DEFAULT);
	}
	
	
	/***
	 * get the font for call-site glyph (the call-site symbol).
	 * @return
	 */
	static public Font getCallsiteGlyphFont() {
		Font font = getFont(PreferenceConstants.ID_FONT_CALLSITE, null);
		if (font == null) {
			return getCallsiteGlyphDefaultFont();
		}
		return font;
	}

	
	static private Font getFont(String id, Font fontDefault) {
		Font font = null;
		try {
			font = INSTANCE.getPreferenceFont(id);
		} catch (Exception e) {
			font = fontDefault;
		}
		return font;
	}

	/****
	 * Retrieve font data of a given font preference.
	 * @see PreferenceConstants.ID_FONT_GENERIC
	 * @see PreferenceConstants.ID_FONT_METRIC
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
		
		// update the cache in the font registry
		// have to do this manually to reflect the current change
		INSTANCE.fontRegistry.put(fontPreferenceID, fontData);

		PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();		
		PreferenceConverter.setValue(pref, fontPreferenceID, fontData);
		pref.save();
	}
	
	
	/*****
	 * Changing the font height of generic, metric, text and call site characters
	 * 
	 * @param
	 *  deltaHeight the delta of the height. If delta < 0, it will decrement the height,
	 *  if delta > 0, it increments it.	 
	 */
	static public void changeFontHeight(int deltaHeight) {
		changeFontHeight(PreferenceConstants.ID_FONT_GENERIC,  deltaHeight);
		changeFontHeight(PreferenceConstants.ID_FONT_METRIC,   deltaHeight);
		changeFontHeight(PreferenceConstants.ID_FONT_TEXT,     deltaHeight);
		changeFontHeight(PreferenceConstants.ID_FONT_CALLSITE, deltaHeight);
	}
	
	/****
	 * Change the height of the font for a given font id from the {@link PreferenceConstants}
	 * @param id the font id from {@link PreferenceConstants}
	 * @param deltaHeight the number of increase/decrease
	 */
	static public void changeFontHeight(String id, int deltaHeight) {
		FontData []oldfd = FontManager.getFontDataPreference(id);
		FontData []newFd = FontDescriptor.copy(oldfd);
		int height = newFd[0].getHeight();
		int heightNew = height+deltaHeight;
		newFd[0].setHeight(heightNew);
		try {
			FontManager.setFontPreference(id, newFd);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
	}

}
