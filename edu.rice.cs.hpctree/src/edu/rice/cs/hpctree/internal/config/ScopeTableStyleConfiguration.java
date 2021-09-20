package edu.rice.cs.hpctree.internal.config;

import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpcsetting.fonts.FontManager;

public class ScopeTableStyleConfiguration extends DefaultNatTableStyleConfiguration 
{
    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
    	if (Display.isSystemDarkTheme()) {
    		bgColor = GUIHelper.COLOR_DARK_GRAY;
    		fgColor = GUIHelper.COLOR_WHITE;
    	}
    	font = FontManager.getFontGeneric();
    	
    	super.configureRegistry(configRegistry);
    }
}
