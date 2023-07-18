package edu.rice.cs.hpcbase;

public class Theme 
{
	private Theme() {
		// nothing
	}
	
	public static boolean isDarkThemeActive() {
		// It's better to not support Dark theme at the moment
		// until we have a better solution to have Eclipse/SWT
		// for dark theme, especially on Linux
		return false;
	}
}
