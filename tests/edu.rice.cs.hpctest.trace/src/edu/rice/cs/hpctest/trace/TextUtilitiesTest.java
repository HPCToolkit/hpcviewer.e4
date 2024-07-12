// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctest.trace;

import static org.junit.Assert.*;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.junit.Test;

import edu.rice.cs.hpctraceviewer.ui.internal.TextUtilities;

public class TextUtilitiesTest {

	@Test
	public void testGetTheRightFontSize() {
		Display display = Display.getDefault();
		Image image = new Image(display, 100, 20);
		GC gc = new GC(image);
		
		// testing different size
		for(int size=image.getBounds().height; size > 5; size--) {
			int currentFontSize = gc.getFont().getFontData()[0].getHeight();
			
			int suggestedSize = TextUtilities.getTheRightFontSize(display, size);
			
			if (suggestedSize >= 0)
				assertTrue(suggestedSize < currentFontSize);
			else 
				assertTrue(currentFontSize > 0);
		}
		gc.dispose();
		image.dispose();
	}

}
