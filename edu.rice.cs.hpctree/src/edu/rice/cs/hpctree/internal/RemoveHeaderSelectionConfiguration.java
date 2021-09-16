package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;

public class RemoveHeaderSelectionConfiguration extends AbstractRegistryConfiguration {

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.unregisterConfigAttribute(CellConfigAttributes.CELL_STYLE, 
												 DisplayMode.SELECT, 
												 GridRegion.COLUMN_HEADER);
	}

}
