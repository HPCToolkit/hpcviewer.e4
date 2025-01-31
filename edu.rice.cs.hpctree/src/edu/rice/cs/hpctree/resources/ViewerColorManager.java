// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctree.resources;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;

import edu.rice.cs.hpcbase.Theme;
import edu.rice.cs.hpcsetting.color.ColorManager;


/********************************************************
 * 
 * Add-on class to manage colors used in the application
 *
 ********************************************************/
public class ViewerColorManager 
{
	static final private String COLOR_TOP = "hpcviewer.COLOR_TOP";
	static final private String COLOR_ACTIVE = "hpcviewer.COLOR_ACTIVE";
	
	static final private int CONTRAST_COLOR = 60;
	
	
	/****
	 * Get the active hyperlink color. 
	 * This method will check if the dark mode is on or not and returns
	 * the corresponding hyperlink color.
	 * 
	 * @apiNote 
	 * 		This method doesn't guarantee if it detects dark mode successfully or not.
	 * 		Please use {@link getActiveColor(Color)}
	 * @return hyperlink color
	 * @deprecated
	 */
	@Deprecated
	public static Color getActiveColor() {
		Color clrActive = GUIHelper.COLOR_BLUE;
		if (Theme.isDarkThemeActive()) {
			ColorRegistry registry = JFaceResources.getColorRegistry();
			clrActive = registry.get(COLOR_ACTIVE);
			if (clrActive == null) {
				clrActive = new Color(51, 153, 255);
				registry.put(COLOR_ACTIVE, clrActive.getRGB());
			}
		}
		return clrActive;
	}
	
	
	/****
	 * Get the active hyperlink color given the background color.
	 * This method is more accurate than {@link getActiveColor} since it detects
	 * if the background color is dark enough or not.
	 * 
	 * @param backgroundColor
	 * @return
	 */
	public static Color getActiveColor(Color backgroundColor) {
		if (ColorManager.getTextFg(backgroundColor) == ColorManager.COLOR_BLACK) {
			// light theme
			return GUIHelper.COLOR_BLUE;
		}
		return getActiveColorDarkMode();
	}
	
	
	private static Color getActiveColorDarkMode() {
		ColorRegistry registry = JFaceResources.getColorRegistry();
		Color clrActive = registry.get(COLOR_ACTIVE);
		if (clrActive == null) {
			clrActive = new Color(51, 153, 255);
			registry.put(COLOR_ACTIVE, clrActive.getRGB());
		}
		return clrActive;
	}
	
	/****
	 * Retrieve the background color of the top row
	 * 
	 * @param parent 
	 * 	the table {@link Control} panel. It is used to know what color
	 * 	is the panel and the background will adapt it. We want the top
	 *  color has contrast color to the panel.
	 *  
	 * @return {@link Color}
	 *   default color for the background
	 */
	public static Color getBgTopRow(Control parent) {
		ColorRegistry registry = JFaceResources.getColorRegistry();
		if (registry == null)
			return null;
		
		Color clrDesc = registry.get(COLOR_TOP);
		if (clrDesc != null) {
			return clrDesc;
		}
		Color background = parent.getBackground();
		RGB rgbBackground = background.getRGB();
		RGB rgb = new RGB(rgbBackground.red, 
						  rgbBackground.green, 
						  Math.abs(rgbBackground.blue  - CONTRAST_COLOR));
		Color topColor = new Color(parent.getDisplay(), rgb);
		registry.put(COLOR_TOP, rgb);
		
		return topColor;
	}
	

	/*****
	 * Get the foreground color of the top row of the table.
	 * This method will check the color of the background (see {@link getBgTopRow}
	 * and then adjust the color for the foreground.
	 * 
	 * @param parent
	 * @return
	 */
	public static Color getFgTopRow(Control parent) {
		Color bg = getBgTopRow(parent);
		return ColorManager.getTextFg(bg);
	}
}
