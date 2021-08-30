package edu.rice.cs.hpctree.internal;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Font;

import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpctree.resources.IconManager;

public class TableConfiguration implements IConfiguration 
{

	@Override
	public void configureLayer(ILayer layer) {}

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		addIconLabel(configRegistry, IconManager.Image_CallTo, ScopeTreeLabelAccumulator.LABEL_CALLSITE);
		addIconLabel(configRegistry, IconManager.Image_CallToDisabled, ScopeTreeLabelAccumulator.LABEL_CALLSITE_DISABLED);

		addIconLabel(configRegistry, IconManager.Image_CallFrom, ScopeTreeLabelAccumulator.LABEL_CALLER);
		addIconLabel(configRegistry, IconManager.Image_CallFromDisabled, ScopeTreeLabelAccumulator.LABEL_CALLER_DISABLED);
		
		// configuration for metric column
		//
		final Font fontMetric  = getMetricFont();
		final Style styleMetric = new Style();
		styleMetric.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
		styleMetric.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
		styleMetric.setAttributeValue(CellStyleAttributes.FONT, fontMetric);
		
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
											   styleMetric, 
											   DisplayMode.NORMAL, 
											   ScopeTreeLabelAccumulator.LABEL_METRICOLUMN);
		
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
											   styleMetric, 
											   DisplayMode.SELECT, 
											   ScopeTreeLabelAccumulator.LABEL_METRICOLUMN);

		// configuration for tree column
		//
		final Font fontGeneric = getGenericFont();
		final Style styleTree  = new Style();
		styleTree.setAttributeValue(CellStyleAttributes.FONT, fontGeneric);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
											   styleTree, 
											   DisplayMode.NORMAL, 
											   ScopeTreeLabelAccumulator.LABEL_TREECOLUMN);
		
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
				   							   styleTree, 
											   DisplayMode.SELECT, 
											   ScopeTreeLabelAccumulator.LABEL_TREECOLUMN);

		final Style styleActive = new Style();
		styleActive.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.COLOR_BLUE);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
					styleActive, 
					DisplayMode.NORMAL, 
					ScopeTreeLabelAccumulator.LABEL_SOURCE_AVAILABLE);
	}

	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {}
	
	
	private void addIconLabel(IConfigRegistry configRegistry, String imageName, String label) {
		IconManager iconManager = IconManager.getInstance();
		
		ImagePainter imagePainter = new ImagePainter(iconManager.getImage(imageName));
		CellPainterDecorator cellPainter = new CellPainterDecorator(new TextPainter(), CellEdgeEnum.LEFT, imagePainter);
		
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, 
											   cellPainter, 
											   DisplayMode.NORMAL, 
											   label);
		
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, 
											   cellPainter, 
											   DisplayMode.SELECT, 
											   label);
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
