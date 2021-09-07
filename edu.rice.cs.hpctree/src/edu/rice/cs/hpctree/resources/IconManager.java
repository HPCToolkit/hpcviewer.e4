package edu.rice.cs.hpctree.resources;

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
	
	
	public static IconManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new IconManager();
		}
		return INSTANCE;
	}
}
