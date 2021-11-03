package edu.rice.cs.hpcsetting.table;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.style.theme.DarkNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import edu.rice.cs.hpcsetting.color.ColorManager;

public class DarkThemeConfiguration extends DarkNatTableThemeConfiguration 
{
	public DarkThemeConfiguration(final NatTable nattable) {
		super();
		
		// Fix issue #120: the color has to make the line separator visible
		// so far dark gray is good enough, for most themes (to be verified)
		//cHeaderBgColor = GUIHelper.COLOR_WHITE;
		cHeaderFgColor = ColorManager.getTextFg(cHeaderBgColor);
		
		cHeaderSelectionBgColor = GUIHelper.COLOR_DARK_GRAY;
		cHeaderSelectionFgColor = GUIHelper.COLOR_WHITE;
		
		defaultSelectionFgColor = ColorManager.getTextFg(defaultSelectionBgColor);
        selectionAnchorSelectionBgColor = defaultSelectionBgColor;
        selectionAnchorSelectionFgColor = defaultSelectionFgColor;

		// Fix issue #132 more contrast is needed for color selection
        defaultBgColor = nattable.getBackground();
		defaultFgColor = ColorManager.getTextFg(this.defaultBgColor);
		rHeaderFgColor = ColorManager.getTextFg(this.rHeaderBgColor);
	}
}
