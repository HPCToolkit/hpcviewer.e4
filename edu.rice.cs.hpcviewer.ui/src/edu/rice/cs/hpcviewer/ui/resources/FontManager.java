package edu.rice.cs.hpcviewer.ui.resources;

import javax.inject.Singleton;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import edu.rice.cs.hpcviewer.ui.preferences.PreferenceConstants;

@Creatable
@Singleton
public class FontManager 
{
	// list of available fixed fonts.
	// the order is important: if the font in first element is available, then we'll use this one.
	static public enum FontID {FONT_GENERIC, FONT_METRIC};
		

	/****
	 * Initialize the font.
	 * The application has to call this method early at the start-up 
	 */
	public FontManager() {
		
		Font fontDefault = JFaceResources.getDefaultFont();
		initFont(PreferenceConstants.P_FONT_GENERIC, fontDefault, 0);
		
		fontDefault = JFaceResources.getTextFont();
		
		// On some platforms, the default text size is a bit bigger.
		// we need to make it smaller to fit the table row :-(
		
		initFont(PreferenceConstants.P_FONT_METRIC, fontDefault, -1);
	}

	private void initFont(String name, Font defaultFont, int deltaHeight) {
		
		IEclipsePreferences prefViewer = InstanceScope.INSTANCE.getNode(PreferenceConstants.P_HPCVIEWER);

		FontData []fd = defaultFont.getFontData();

		String fontName = prefViewer.get(name, null);
		if (fontName  == null) {

			FontData defaultFD = fd[0];

			if (deltaHeight != 0) {
				// need to adapt the height
				FontData copyFd = FontDescriptor.copy(fd[0]);
				defaultFD = copyFd;
				int defaultHeight = defaultFD.getHeight() + deltaHeight;
				defaultFD.setHeight(defaultHeight);
			}
			prefViewer.put(name, defaultFD.name);
			
			fd[0] = defaultFD;
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
		return registry.get(PreferenceConstants.P_FONT_GENERIC);
	}
	
	/***
	 * get the fixed font for metrics
	 * @return
	 */
	static public Font getMetricFont() {
		FontRegistry registry = JFaceResources.getFontRegistry();
		return registry.get(PreferenceConstants.P_FONT_METRIC);
	}
	
	
	/***
	 * get the name of the preference
	 * @param fontID
	 * @return
	 */
	static private String getPreferenceName(FontID fontID) {
		String fontPrefName = null;
		
		switch (fontID) {
		case FONT_GENERIC:
			fontPrefName = PreferenceConstants.P_FONT_GENERIC;
			break;
		case FONT_METRIC:
			fontPrefName = PreferenceConstants.P_FONT_METRIC;
			break;
		default:
			fontPrefName = PreferenceConstants.P_FONT_GENERIC;
			break;
		}
		return fontPrefName;
	}

	/***
	 * get the font data based on the user's preference ID
	 * @param fontID {@code FontID}
	 * @return
	 */
	static public FontData[] getFontDataPreference(FontID fontID) {
		String name = getPreferenceName(fontID);
		return getFontDataPreference(name);
	}

	
	static private FontData[] getFontDataPreference(String fontPreferenceID) {
		IEclipsePreferences prefViewer = InstanceScope.INSTANCE.getNode(PreferenceConstants.P_HPCVIEWER);
		if (prefViewer != null) {
			String fontName = prefViewer.get(fontPreferenceID, null);
			if (fontName  != null) {
				FontDescriptor desc = FontDescriptor.createFrom(fontName, 10, SWT.NORMAL);
				return desc.getFontData();
			}
		}
		return null;
	}
    
	
	static public void setFontPreference(String fontPreferenceID, FontData[] fontData) {
	}
}
