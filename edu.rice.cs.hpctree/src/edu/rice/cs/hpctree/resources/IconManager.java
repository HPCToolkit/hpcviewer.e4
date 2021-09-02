package edu.rice.cs.hpctree.resources;

import org.eclipse.jface.resource.ImageRegistry;
import edu.rice.cs.hpcbase.ui.BaseIconManager;

public class IconManager extends BaseIconManager 
{
	final static public String Image_CallFrom = "CallFrom.gif";
	final static public String Image_CallTo = "CallTo.gif";
	final static public String Image_CallFromDisabled = "CallFromDisabled.gif";
	final static public String Image_CallToDisabled = "CallToDisabled.gif";
	
	final static public String Image_InlineFrom = "CallFromInline.gif";
	final static public String Image_InlineTo = "CallToInline.gif";
	final static public String Image_InlineFromDisabled = "CallFromInlineDisabled.gif";
	final static public String Image_InlineToDisabled = "CallToInlineDisabled.gif";
	
	private static IconManager INSTANCE;
	
	public IconManager() {
		ImageRegistry registry = getRegistry();
/*
		registerImage(registry, getClass(), Image_CallFrom); 
		registerImage(registry, getClass(), Image_CallTo); 
		registerImage(registry, getClass(), Image_CallFromDisabled); 
		registerImage(registry, getClass(), Image_CallToDisabled);

		registerImage(registry, getClass(), Image_InlineFrom);
		registerImage(registry, getClass(), Image_InlineTo); 
		registerImage(registry, getClass(), Image_InlineFromDisabled); 
		registerImage(registry, getClass(), Image_InlineToDisabled);
		*/
	}
	
	public static IconManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new IconManager();
		}
		return INSTANCE;
	}
}
