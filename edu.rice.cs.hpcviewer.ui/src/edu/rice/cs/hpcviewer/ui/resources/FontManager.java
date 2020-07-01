package edu.rice.cs.hpcviewer.ui.resources;

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import edu.rice.cs.hpc.data.util.OSValidator;
import edu.rice.cs.hpcviewer.ui.preferences.PreferenceConstants;
import edu.rice.cs.hpcviewer.ui.preferences.ViewerPreferenceManager;

@Creatable
@Singleton
public class FontManager 
{
	
	/****
	 * Initialize the font.
	 * The application has to call this method early at the start-up 
	 */
	public FontManager() {
		
		Font fontDefault = JFaceResources.getDefaultFont();
		initFont(PreferenceConstants.ID_FONT_GENERIC, fontDefault, 0);
		
		// special case for text fonts:
		// On some platforms, the default text size is a bit bigger.
		// we need to make it smaller to fit the table row :-(
		
		int fontHeightDecrease = 0;
		if (OSValidator.isUnix())
			fontHeightDecrease = -1;
		
		fontDefault = JFaceResources.getTextFont();
		initFont(PreferenceConstants.ID_FONT_METRIC, fontDefault, fontHeightDecrease);
		
		initFont(PreferenceConstants.ID_FONT_TEXT, fontDefault, 0);
	}

	private void initFont(String name, Font defaultFont, int deltaHeight) {

		FontData []fd = getFontDataPreference(name);
		if (fd == null || fd.length == 0) {
			fd = defaultFont.getFontData();
			setFontPreference(name, fd);
		}
		if (deltaHeight != 0) {
			int height = fd[0].getHeight() + deltaHeight;
			fd[0].setHeight(height);
		}
		
		FontRegistry registry = JFaceResources.getFontRegistry();
		registry.put(name, fd);
	}
	
	/***
	 * get the font for generic text
	 * @return
	 */
	static public Font getFontGeneric() {
		FontRegistry registry = JFaceResources.getFontRegistry();
		return registry.get(PreferenceConstants.ID_FONT_GENERIC);
	}
	
	
	/***
	 * get the fixed font for metrics
	 * @return
	 */
	static public Font getMetricFont() {
		FontRegistry registry = JFaceResources.getFontRegistry();
		return registry.get(PreferenceConstants.ID_FONT_METRIC);
	}	
	
	
	/***
	 * get the fixed font for metrics
	 * @return
	 */
	static public Font getTextEditorFont() {
		FontRegistry registry = JFaceResources.getFontRegistry();
		return registry.get(PreferenceConstants.ID_FONT_TEXT);
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
    
	
	static public void setFontPreference(String fontPreferenceID, FontData[] fontData) {

		IPreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		
		PreferenceConverter.setValue(pref, fontPreferenceID, fontData);

	}
}
