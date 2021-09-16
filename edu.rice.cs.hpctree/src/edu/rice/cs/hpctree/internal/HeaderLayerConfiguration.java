package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;

public class HeaderLayerConfiguration implements IConfiguration, IConfigLabelAccumulator 
{
	private final static String LABEL_COLUMN = "col";

	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {}
	
	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		final Style style = new Style();
		style.setAttributeValue(CellStyleAttributes.FONT, TableConfiguration.getGenericFont());

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.SELECT, LABEL_COLUMN);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL, LABEL_COLUMN);
	}
	
	@Override
	public void configureLayer(ILayer layer) {}

	
	@Override
	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		configLabels.add(LABEL_COLUMN);
	}

}
