package edu.rice.cs.hpctree.internal;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.swt.graphics.Font;

import edu.rice.cs.hpcsetting.fonts.FontManager;

public class MetricTableRegistryConfiguration extends AbstractRegistryConfiguration {

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		
		final Font fontMetric  = getMetricFont();
		final Style styleMetric = new Style();
		styleMetric.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
		styleMetric.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
		styleMetric.setAttributeValue(CellStyleAttributes.FONT, fontMetric);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, styleMetric, DisplayMode.NORMAL, TableConfigLabelProvider.LABEL_METRICOLUMN);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, styleMetric, DisplayMode.SELECT, TableConfigLabelProvider.LABEL_METRICOLUMN);

		final Font fontGeneric = getGenericFont();
		final Style styleTree  = new Style();
		styleTree.setAttributeValue(CellStyleAttributes.FONT, fontGeneric);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
											   styleTree, 
											   DisplayMode.NORMAL, 
											   TableConfigLabelProvider.LABEL_TREECOLUMN);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
				   							   styleTree, 
											   DisplayMode.SELECT, 
											   TableConfigLabelProvider.LABEL_TREECOLUMN);
	}

	
	public static Font getMetricFont() {
		Font font ;
		try {
			font = FontManager.getMetricFont();
		} catch (Exception e) {
			font = JFaceResources.getTextFont();
		}
		return font;
	}

	public static Font getGenericFont() {
		Font font;
		try {
			font = FontManager.getFontGeneric();
		} catch (Exception e) {
			font = JFaceResources.getDefaultFont();
		}
		return font;
	}
}
