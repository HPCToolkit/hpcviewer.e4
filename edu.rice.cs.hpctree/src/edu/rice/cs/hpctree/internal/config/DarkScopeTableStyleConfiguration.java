package edu.rice.cs.hpctree.internal.config;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.theme.DarkNatTableThemeConfiguration;

import edu.rice.cs.hpcsetting.fonts.FontManager;

public class DarkScopeTableStyleConfiguration extends DarkNatTableThemeConfiguration 
{
    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
    	super.configureRegistry(configRegistry);
    	cHeaderFont = FontManager.getFontGeneric();
    	cHeaderSelectionFont = FontManager.getFontGeneric();
    	defaultSelectionFont = FontManager.getFontGeneric();
    }
}
