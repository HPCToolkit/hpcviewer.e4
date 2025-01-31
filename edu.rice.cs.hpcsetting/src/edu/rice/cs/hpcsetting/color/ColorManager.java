// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcsetting.color;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpcbase.BaseConstants;

public class ColorManager 
{
	public static final Color COLOR_WHITE = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    public static final Color COLOR_BLACK = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
	public static final Color COLOR_ARROW_ACTIVE = new Color(new RGB(255, 69, 0));

    public static Color getTextFg(Color bg) {

		// Pick the color of the text indicating sample depth. 
		// If the background is suffciently light, pick black, otherwise white
		if (bg.getRed()+bg.getBlue()+bg.getGreen()>BaseConstants.DARKEST_COLOR_FOR_BLACK_TEXT) {
			return COLOR_BLACK;
		}
		return COLOR_WHITE;
	}
}
