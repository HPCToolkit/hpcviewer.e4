package edu.rice.cs.hpcviewer.preferences;

import java.awt.GraphicsEnvironment;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class FontManager 
{
	// list of available fixed fonts.
	// the order is important: if the font in first element is available, then we'll use this one.
	static public enum FontID {FONT_GENERIC, FONT_METRIC};
	
	static private final String []LIST_METRIC_FONTS = { "Monospaced", "Courier", "Courier New"};
	

	static public void init() {
		String fontName = LIST_METRIC_FONTS[0];
		
		IEclipsePreferences prefViewer = InstanceScope.INSTANCE.getNode(PreferenceConstants.P_HPCVIEWER);
		if (prefViewer != null) {
			fontName = prefViewer.get(PreferenceConstants.P_FONT_GENERIC, LIST_METRIC_FONTS[0]);
			if (fontName  != null) {
				
			}
		}
		FontRegistry registry = JFaceResources.getFontRegistry();
		registry.put(PreferenceConstants.P_FONT_GENERIC, null);
	}
	
	
	/***
	 * Get the current font
	 * 
	 * @param fontID Type of font. See FontID.FONT_GENERIC or FontID.FONT_METRIC
	 * 
	 * @return font
	 */
	static public Font getFont(FontID fontID) {
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
		// check the font registry. If the font exists, we return whatever in the registry
		// otherwise we check in the preference
		
		FontRegistry registry = JFaceResources.getFontRegistry();
		Font font = registry.get(fontPrefName);
		if (font != null)
			return font;
		
		// check the name of the font in the preference
		FontData fdata[] = getFontDataPreference(fontPrefName);
		if (fdata != null) {
			FontDescriptor desc = FontDescriptor.createFrom(fdata);
			registry.put(fontPrefName, fdata);
			
			Device device = Display.getDefault();
			font = desc.createFont(device);

			return font;
		}
		// no font preference, try to set the font with the default font
		
		if (fontID == FontID.FONT_METRIC) {
			String fontName = getMetricFont();
			font  = registry.defaultFont();
			fdata = font.getFontData();
			fdata[0].setName(fontName);
			
			registry.put(fontPrefName, fdata);
			
			FontDescriptor desc = FontDescriptor.createFrom(fdata);
			return desc.createFont(Display.getDefault());
		}
		return registry.defaultFont();
	}

	
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
	
	
    /***
     * get the best way to find fixed font for metric columns
     * If no font is supported, it will return the first element in the LIST_METRIC_FONTS
     * since Eclipse requires a name. Doesn't matter if it exists or not.
     * 
     * @return fixed font
     */
    static private String getMetricFont() {
		String []availableFonts = getAvailableFonts();
		
		for (String font : LIST_METRIC_FONTS) {
			for (String availableFont : availableFonts) {
				if (font.equals(availableFont)) {
					return availableFont;
				}
			}
		}
		// we don't find any font supported by the system.
		// since we cannot return null (Eclipse doesn't like null font)
		// we just return the first font in the list. 
		// Let Eclipse find out what is the best font for us.
		return LIST_METRIC_FONTS[0];
    }
    
    /***
     * Returns the list of supported font by the system.
     * Since Eclipse 3.x doesn't support this function, we use Java AWT.
     * Once we move to Eclipse 4, we can use its theme/css function.
     *  
     * @return list of supported fonts
     */
    static private String[] getAvailableFonts() {

    	GraphicsEnvironment g=GraphicsEnvironment.getLocalGraphicsEnvironment();
    	return g.getAvailableFontFamilyNames();
    }

}
