package edu.rice.cs.hpcsetting.color;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpcbase.BaseConstants;

public class ColorManager 
{
	public final static Color COLOR_WHITE = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    public final static Color COLOR_BLACK = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);

    public static Color getTextFg(Color bg) {

		// Pick the color of the text indicating sample depth. 
		// If the background is suffciently light, pick black, otherwise white
		if (bg.getRed()+bg.getBlue()+bg.getGreen()>BaseConstants.DARKEST_COLOR_FOR_BLACK_TEXT) {
			return COLOR_BLACK;
		}
		return COLOR_WHITE;
	}
}
