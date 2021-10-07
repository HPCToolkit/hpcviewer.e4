package edu.rice.cs.hpctree.resources;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;

import edu.rice.cs.hpcbase.ui.BaseIconManager;

public class IconManager extends BaseIconManager 
{
	final static public String Image_CallFrom = "CallFrom.gif";
	final static public String Image_CallTo = "CallTo.gif";
	final static public String Image_CallFromDisabled = "CallFromDisabled.gif";
	final static public String Image_CallToDisabled = "CallToDisabled.gif";

	
	private static IconManager INSTANCE;
	
	public IconManager() {
		ImageRegistry registry = JFaceResources.getImageRegistry();
		registerImage(registry, getClass(), Image_CallFrom); 
		registerImage(registry, getClass(), Image_CallTo); 
		registerImage(registry, getClass(), Image_CallFromDisabled); 
		registerImage(registry, getClass(), Image_CallToDisabled); 
	}
	
	public static IconManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new IconManager();
		}
		return INSTANCE;
	}
}
