package edu.rice.cs.hpcbase;

import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpcdata.util.OSValidator;

public class Theme 
{
	private Theme() {
		// nothing
	}
	
	public static boolean isDarkThemeActive() {
		if (OSValidator.isMac())
			return false;
		
		return Display.isSystemDarkTheme();
	}
}
