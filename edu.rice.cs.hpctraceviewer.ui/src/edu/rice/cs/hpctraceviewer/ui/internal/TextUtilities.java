// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.internal;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;

public class TextUtilities 
{
	/***
	 * Compute the right font size given the maximum size of the font
	 * 
	 * @param device
	 * 			The current device
	 * @param gc
	 * 			The graphic context for the font
	 * @param maxHeight
	 * 			The maximum height of the font
	 * 
	 * @return {@code int} 
	 * 			Returns negative number if no change is needed, positive number for the suggested font size
	 */
	public static int getTheRightFontSize(Device device, int maxHeight) {
		
		GC gc = new GC(device);
		Font font = gc.getFont();
		FontData []fd = font.getFontData();
		int currentHeight = fd[0].getHeight();
		int originalHeight = currentHeight;
		int currentFontSize = gc.getFontMetrics().getHeight();
		FontDescriptor descriptor = FontDescriptor.createFrom(font);
		
		while(currentFontSize > maxHeight && currentHeight > 0) {
			currentHeight--;
			
			Font font2 = descriptor.increaseHeight(-1).createFont(device);
			
			gc.setFont(font2);
			
			currentFontSize = gc.getFontMetrics().getHeight();
			
			descriptor.destroyFont(font2);
		}
		gc.dispose();
		
		if (originalHeight == currentHeight)
			return -1;
		
		return currentHeight;
	}
}
