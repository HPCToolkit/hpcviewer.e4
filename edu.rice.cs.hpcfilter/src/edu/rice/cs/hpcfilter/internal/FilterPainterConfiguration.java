package edu.rice.cs.hpcfilter.internal;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Font;

import edu.rice.cs.hpcsetting.fonts.FontManager;

public class FilterPainterConfiguration 
	extends AbstractRegistryConfiguration 
{

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		
		// gray colors for disabled metrics
		//
		final Style styleGray = new Style();
		styleGray.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.COLOR_GRAY);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
											   styleGray, 
											   DisplayMode.NORMAL, 
											   IConstants.LABEL_ROW_GRAY);
		
		
		// configuration for check column: center justified
		//
		final Style styleCenter = new Style();
		styleCenter.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
		styleCenter.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
		
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
											   styleCenter, DisplayMode.NORMAL, 
											   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + IConstants.INDEX_VISIBILITY);
		
		// configuration for name and description columns:
		// - left justified
		// - generic fonts
		//
		final Font fontGeneric = FontManager.getFontGeneric();
		final Style styleLeft = new Style();
		styleLeft.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
		styleLeft.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
		styleLeft.setAttributeValue(CellStyleAttributes.FONT, fontGeneric);
		
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
											   styleLeft, 
											   DisplayMode.NORMAL, 
											   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + IConstants.INDEX_NAME);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
											   styleLeft, 
											   DisplayMode.SELECT, 
											   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + IConstants.INDEX_NAME);
	}
}
