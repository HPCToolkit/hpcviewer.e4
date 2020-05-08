package edu.rice.cs.hpcviewer.ui.resources;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.rice.cs.hpcbase.ui.BaseIconManager;
import edu.rice.cs.hpcviewer.ui.Activator;

/**
 * Singleton class containing global variables for icons 
 * @author laksono
 *
 */
public class Icons  extends BaseIconManager
{
	final static public String Image_CallFrom = "CallFrom.gif";
	final static public String Image_CallTo = "CallTo.gif";
	final static public String Image_CallFromDisabled = "CallFromDisabled.gif";
	final static public String Image_CallToDisabled = "CallToDisabled.gif";
	
	final static public String Image_InlineFrom = "CallFromInline.gif";
	final static public String Image_InlineTo = "CallToInline.gif";
	final static public String Image_InlineFromDisabled = "CallFromInlineDisabled.gif";
	final static public String Image_InlineToDisabled = "CallToInlineDisabled.gif";
	
	final static public String Image_MetricAggregate  = "MetricAggregate.png";
	
	final static public String Image_ZoomIn = "ZoomIn.gif";
	final static public String Image_ZoomOut = "ZoomOut.gif";
	final static public String Image_Flatten = "Flatten.gif";
	final static public String Image_Unflatten = "Unflatten.gif";
	
	final static public String Image_CheckColumns = "checkColumns.gif";
	final static public String Image_FlameIcon = "flameIcon.gif";
	
	final static public String Image_FnMetric = "FnMetric.gif";
	final static public String Image_FontBigger = "FontBigger.gif";
	final static public String Image_FontSmaller = "FontSmaller.gif";
	final static public String Image_SaveCSV = "savecsv.gif";
	final static public String Image_Graph = "Graph.png";
	final static public String Image_ThreadView = "cct-thread.png";
	final static public String Image_ThreadMap = "cct-thread-map.png";
	
	static private Icons __singleton=null;
	static private final AtomicBoolean isInitialized = new AtomicBoolean(false);
	
	/********
	 * get the instance of the current icons class
	 * We just want to make sure only ONE initialization for 
	 * image creation and registration for each class.
	 * 
	 * Although initialization for each object doesn't harm, but
	 * it's useless and time consuming 
	 * 
	 * @return
	 ********/
	static public Icons getInstance() {
		if (Icons.__singleton == null) {
			Icons.__singleton = new Icons();
		}
		return Icons.__singleton;
	}	
	
	/*************
	 * initialize images. The method only needs to be called once for the whole
	 * window lifespan. Athough calling this multiple times is theoretically
	 * harmless (never tried).
	 * 
	 * @param registry
	 *************/
	public void init(ImageRegistry registry) {
		
		if (isInitialized.compareAndSet(false,
				true)) {/*
						 * registerImage(registry, getClass(), Image_CallFrom); registerImage(registry,
						 * getClass(), Image_CallTo); registerImage(registry, getClass(),
						 * Image_CallFromDisabled); registerImage(registry, getClass(),
						 * Image_CallToDisabled);
						 * 
						 * registerImage(registry, getClass(), Image_InlineFrom);
						 * registerImage(registry, getClass(), Image_InlineTo); registerImage(registry,
						 * getClass(), Image_InlineFromDisabled); registerImage(registry, getClass(),
						 * Image_InlineToDisabled);
						 * 
						 * registerImage(registry, getClass(), Image_MetricAggregate);
						 * 
						 * registerImage(registry, getClass(), Image_ZoomIn); registerImage(registry,
						 * getClass(), Image_ZoomOut); registerImage(registry, getClass(),
						 * Image_Flatten); registerImage(registry, getClass(), Image_Unflatten);
						 * 
						 * registerImage(registry, getClass(), Image_CheckColumns);
						 * registerImage(registry, getClass(), Image_FlameIcon);
						 * 
						 * registerImage(registry, getClass(), Image_FnMetric); registerImage(registry,
						 * getClass(), Image_FontBigger); registerImage(registry, getClass(),
						 * Image_FontSmaller); registerImage(registry, getClass(), Image_SaveCSV);
						 * registerImage(registry, getClass(), Image_Graph); registerImage(registry,
						 * getClass(), Image_ThreadView); registerImage(registry, getClass(),
						 * Image_ThreadMap);
						 */
		}
	}
	
	
	/*
	 * static public Image getImage(final String desc) { AbstractUIPlugin plugin =
	 * Activator.getDefault(); return getImage(plugin, desc); }
	 */
}
