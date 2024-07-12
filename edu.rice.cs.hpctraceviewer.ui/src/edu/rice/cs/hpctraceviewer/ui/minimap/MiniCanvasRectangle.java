// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.ui.minimap;

import java.util.EnumSet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;



public class MiniCanvasRectangle {
	final EnumSet<SampleHiddenReason> reasons;
	final boolean areAllSamplesHidden;
	final Rectangle rect;
	Image img; // Stored so it can be disposed
	
	public MiniCanvasRectangle (SampleHiddenReason hiddenReason, boolean _areAllSamplesHidden, Rectangle _rect) {
		this(EnumSet.of(hiddenReason), _areAllSamplesHidden, _rect);
	}

	
	public MiniCanvasRectangle (EnumSet<SampleHiddenReason> hiddenReasons, boolean _areAllSamplesHidden, Rectangle _rect) {
		reasons = hiddenReasons;
		areAllSamplesHidden = _areAllSamplesHidden;
		rect = _rect;
		createLabel();
	}
	
	/**
	 * Takes a list of MiniCanvasRectangles that may intersect and produces a modified list that 
	 * is easier to draw because none of the rectangles intersect. The regions that originally
	 * intersect become their own rectangle with a combination of the reasons.
	 */
	/*public static ArrayList<MiniCanvasRectangle> CreateFromIntersections(ArrayList<MiniCanvasRectangle> pieces) {
		
		for (int i = 0; i < pieces.size(); i++) {
			for (int j = i + 1; j < pieces.size(); j++) {
				if (pieces.get(i).rect.intersects(pieces.get(j).rect)){
					
				}
			}
		}
	}*/

	private void createLabel() {
		// bug fix: when the zoom-in is very huge, the width or the height can be zero
		// 			since SWT image doesn't allow us to create a zero width/height,
		//			we just ignore the request the create the label
		if (rect.width > 0 && rect.height>0) {
			img = new Image(Display.getCurrent(), rect.width, rect.height);
			GC gc = new GC(img);
			gc.setForeground(getColor());
			// Because we can't draw with transparency, we need the label to be exactly
			// the right size, and then we will place it later.
			gc.drawRectangle(new Rectangle(0, 0, rect.width, rect.height));
			gc.dispose();
		}
	}

	private Color getColor() {
		// Average the colors
		int r = 0, g = 0, b = 0;
		for (SampleHiddenReason reason : reasons) {
			r += reason.color.getRed();
			g += reason.color.getGreen();
			b += reason.color.getBlue();
		}
		r /= reasons.size();
		g /= reasons.size();
		b /= reasons.size();
		return new Color(Display.getCurrent(), r, g, b);
	}

	/** The control is a label. Not intuitive at all, but label allows
	 *  us to specify an image and have a tooltip. */
	public Control getControl(Composite parent) {
		 Label label = new Label(parent, SWT.NONE);
		 label.setImage(img);
		 label.setToolTipText(getAsMessage());
		 label.setLocation(rect.x, rect.y);
		 label.pack();
		 return label;
	}

	private String getAsMessage() {
		if ((reasons.size() == 0) || 
				((reasons.size()==1) && (reasons.contains(SampleHiddenReason.NONE)))) {
			return "All samples from this region are displayed.";
		}
		StringBuilder sb = new StringBuilder();
		if (areAllSamplesHidden)
			sb.append("All");
		else
			sb.append("Some");
		sb.append(" samples from this region ");
		if (areAllSamplesHidden)
			sb.append("are");
		else
			sb.append("may be");
		sb.append(" hidden");
		
		int i = 0;
		for (SampleHiddenReason reason : reasons) {
			switch (reason) {
			case OUTSIDE_BOUNDS:
				sb.append(" because they are outside the currently selected time "
						+ "and process bounds");
				break;
			case FILTERED:
				sb.append(" because they do not match the current filter");
				break;
			case VERTICAL_RESOLUTION:
				sb.append(" because the current vertical resolution is too small "
						+ "to show all selected traces");
			case NONE:
				break;
			default:
				break;
			}
			if (reasons.size() > 1){
				if (i < reasons.size() - 2)
					sb.append(",");
				else
					sb.append(", and");
			}
		}

		sb.append(".");
		return sb.toString();
	}

	public void dispose() {
		if (img != null)
			img.dispose();
	}
}

enum SampleHiddenReason {
	NONE (SWT.COLOR_WHITE),
	OUTSIDE_BOUNDS (SWT.COLOR_DARK_GRAY),
	FILTERED (SWT.COLOR_DARK_BLUE),
	VERTICAL_RESOLUTION (new Color(Display.getCurrent(), 170, 170, 170)); //Light gray
	final Color color;
	private SampleHiddenReason(Color _color) {
		color = _color;
	}
	private SampleHiddenReason(int _color) {
		color = Display.getCurrent().getSystemColor(_color);
	}
}