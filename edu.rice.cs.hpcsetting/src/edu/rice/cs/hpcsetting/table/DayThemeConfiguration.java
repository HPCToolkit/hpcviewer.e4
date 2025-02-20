// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcsetting.table;

import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;

import edu.rice.cs.hpcsetting.color.ColorManager;

public class DayThemeConfiguration extends ModernNatTableThemeConfiguration 
{

	public DayThemeConfiguration() {
		super();
		// Fix issue #120: the color has to make the line separator visible
		// so far dark gray is good enough, for most themes (to be verified)
		cHeaderBgColor = GUIHelper.COLOR_DARK_GRAY;
		cHeaderFgColor = GUIHelper.COLOR_WHITE;

		// Fix issue #132 more contrast is needed for color selection
		defaultFgColor = ColorManager.getTextFg(this.defaultBgColor);
        defaultSelectionFgColor = ColorManager.getTextFg(this.defaultSelectionBgColor);

        selectionAnchorFgColor  = defaultSelectionFgColor;
        selectionAnchorBgColor  = defaultSelectionBgColor;
		
        selectionAnchorSelectionBgColor = defaultSelectionBgColor;
        selectionAnchorSelectionFgColor = defaultSelectionFgColor;
        
		cHeaderSelectionBgColor = GUIHelper.COLOR_DARK_GRAY;
		cHeaderSelectionFgColor = ColorManager.getTextFg(cHeaderSelectionBgColor);
	}
}
