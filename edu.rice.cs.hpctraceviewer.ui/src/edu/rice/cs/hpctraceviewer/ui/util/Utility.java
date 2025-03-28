// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.util;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**********************************************
 * 
 * Class to provide generic utility methods
 *
 **********************************************/
public class Utility 
{	
	/****
	 * Retrieve an image based on the registry label, or if the image
	 * is not in the registry, load it from file URL
	 * 
	 * @param fileURL the URL
	 * @param label the registry label
	 * 
	 * @return image, null if the file doesn't exist
	 */
	public static Image getImage(String fileURL, String label) {
		
		Image image = JFaceResources.getImageRegistry().get(label);
		if (image != null)
			return image;
		
		try {
			URL url = FileLocator.toFileURL(new URL(fileURL));
			image = new Image(Display.getDefault(), url.getFile());
			JFaceResources.getImageRegistry().put(label, image);

		} catch (IOException e1) {
			Logger logger = LoggerFactory.getLogger(Utility.class);
			logger.error("Unable to get the icon file: " + fileURL, e1);
		}
		return image;
	}

}
