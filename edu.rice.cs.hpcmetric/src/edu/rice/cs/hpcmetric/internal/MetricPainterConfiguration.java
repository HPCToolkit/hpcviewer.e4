// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcmetric.internal;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.swt.graphics.Font;

import edu.rice.cs.hpcsetting.fonts.FontManager;

public class MetricPainterConfiguration extends AbstractRegistryConfiguration 
{
	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
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
				   							   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + IConstants.INDEX_DESCRIPTION);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
				   							   styleLeft, 
				   							   DisplayMode.SELECT, 
				   							   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + IConstants.INDEX_DESCRIPTION);
		
		// wrap long texts for description column
		//
		TextPainter tp = new TextPainter(true, true, true);			
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, 
											   tp, 
											   DisplayMode.NORMAL, 
											   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + IConstants.INDEX_DESCRIPTION);	

		// right justify for metric columns
		//
		Font font = FontManager.getMetricFont();

		final Style style = new Style();
		style.setAttributeValue(CellStyleAttributes.FONT, font);
		style.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
		style.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
											   style, 
											   DisplayMode.NORMAL, 
											   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + IConstants.INDEX_METRIC_VAL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
				   							   style, 
				   							   DisplayMode.SELECT, 
				   							   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + IConstants.INDEX_METRIC_VAL);
	}
}
