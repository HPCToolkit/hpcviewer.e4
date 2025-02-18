// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcviewer.ui.resources;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import edu.rice.cs.hpcbase.ui.BaseIconManager;

/**
 * Singleton class containing global variables for icons 
 */
public class IconManager  extends BaseIconManager
{
	public static final String Image_ZoomIn = "ZoomIn.gif";
	public static final String Image_ZoomOut = "ZoomOut.gif";
	public static final String Image_Flatten = "Flatten.png";
	public static final String Image_Unflatten = "Unflatten.png";
	
	public static final String Image_CheckColumns = "checkColumns.gif";
	public static final String Image_FlameIcon = "flameIcon.gif";
	public static final String Image_TableFitBoth  = "TableFitBoth.png";
	public static final String Image_TableFitData  = "TableFitData.png";

	public static final String Image_FnMetric = "FnMetric.png";
	public static final String Image_FontBigger = "FontBigger.png";
	public static final String Image_FontSmaller = "FontSmaller.png";
	public static final String Image_SaveCSV = "savecsv.png";
	public static final String Image_Graph = "Graph.png";
	public static final String Image_ThreadView = "cct-thread.png";
	public static final String Image_ThreadMap = "cct-thread-map.png";
	
	public static final String Image_Viewer_256 = "platform:/plugin/edu.rice.cs.hpcviewer.ui/resources/hpcviewerEclipse256.gif";
	public static final String Image_Viewer_128 = "platform:/plugin/edu.rice.cs.hpcviewer.ui/resources/hpcviewerEclipse128.gif";
	public static final String Image_Viewer_64  = "platform:/plugin/edu.rice.cs.hpcviewer.ui/resources/hpcviewerEclipse64.gif";
	public static final String Image_Viewer_48  = "platform:/plugin/edu.rice.cs.hpcviewer.ui/resources/hpcviewerEclipse48.gif";
	public static final String Image_Viewer_32  = "platform:/plugin/edu.rice.cs.hpcviewer.ui/resources/hpcviewerEclipse32.gif";
	public static final String Image_Viewer_16  = "platform:/plugin/edu.rice.cs.hpcviewer.ui/resources/hpcviewerEclipse16.gif";
	
	public static final String []Image_Viewer = {
			Image_Viewer_16,  Image_Viewer_32,
			Image_Viewer_48,  Image_Viewer_64,
			Image_Viewer_128, Image_Viewer_256
	};
	
	private static IconManager __singleton=null;
	private static final AtomicBoolean isInitialized = new AtomicBoolean(false);
	
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
	public static IconManager getInstance() {
		if (IconManager.__singleton == null) {
			IconManager.__singleton = new IconManager();
		}
		return IconManager.__singleton;
	}	
	
	/*************
	 * initialize images. The method only needs to be called once for the whole
	 * window life span. Although calling this multiple times is theoretically
	 * harmless (never tried).
	 * 
	 *************/
	IconManager() {
		
		if (isInitialized.compareAndSet(false,true)) {
			ImageRegistry registry = JFaceResources.getImageRegistry();

			registerImage(registry, getClass(), Image_ZoomIn); 
			registerImage(registry, getClass(), Image_ZoomOut); 
			registerImage(registry, getClass(), Image_Flatten); 
			registerImage(registry, getClass(), Image_Unflatten);

			registerImage(registry, getClass(), Image_CheckColumns);
			registerImage(registry, getClass(), Image_TableFitBoth);
			registerImage(registry, getClass(), Image_TableFitData);
			registerImage(registry, getClass(), Image_FlameIcon);

			registerImage(registry, getClass(), Image_FnMetric); 
			registerImage(registry, getClass(), Image_FontBigger); 
			registerImage(registry, getClass(), Image_FontSmaller); 
			registerImage(registry, getClass(), Image_SaveCSV);
			registerImage(registry, getClass(), Image_Graph); 
			registerImage(registry, getClass(), Image_ThreadView); 
			registerImage(registry, getClass(), Image_ThreadMap);
		}
	}
	
	
	
	@Override
	public Image getImage(final String desc) { 
		ImageRegistry registry = JFaceResources.getImageRegistry();

		return registry.get(desc); 
	}
	 
}
