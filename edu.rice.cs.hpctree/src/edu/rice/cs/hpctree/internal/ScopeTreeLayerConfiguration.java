package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.export.ExportConfigAttributes;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.action.TreeExpandCollapseAction;
import org.eclipse.nebula.widgets.nattable.tree.config.TreeExportFormatter;
import org.eclipse.nebula.widgets.nattable.tree.painter.TreeImagePainter;
import org.eclipse.nebula.widgets.nattable.ui.action.NoOpMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellPainterMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;

import edu.rice.cs.hpctree.ScopeTreeLayer;

public class ScopeTreeLayerConfiguration implements IConfiguration 
{
	private final ScopeTreeLayer treeLayer;
	
	
	public ScopeTreeLayerConfiguration(ScopeTreeLayer treeLayer) {
		this.treeLayer = treeLayer;
	}

	@Override
	public void configureLayer(ILayer layer) {
	}

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		Style style = new Style();
		style.setAttributeValue(
				CellStyleAttributes.HORIZONTAL_ALIGNMENT,
				HorizontalAlignmentEnum.LEFT);

		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_STYLE,
				style,
				DisplayMode.NORMAL,
				ScopeTreeLayer.TREE_COLUMN_CELL);

		configRegistry.registerConfigAttribute(
				ExportConfigAttributes.EXPORT_FORMATTER,
				new TreeExportFormatter(this.treeLayer.getModel()),
				DisplayMode.NORMAL,
				TreeLayer.TREE_COLUMN_CELL);
	}

	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        TreeExpandCollapseAction treeExpandCollapseAction = new TreeExpandCollapseAction();
        
        CellPainterMouseEventMatcher treeImagePainterMouseEventMatcher =
                new CellPainterMouseEventMatcher(
                        GridRegion.BODY,
                        MouseEventMatcher.LEFT_BUTTON,
                        TreeImagePainter.class);

        uiBindingRegistry.registerFirstSingleClickBinding(
                treeImagePainterMouseEventMatcher,
                treeExpandCollapseAction);

        // Obscure any mouse down bindings for this image painter
        uiBindingRegistry.registerFirstMouseDownBinding(
                treeImagePainterMouseEventMatcher,
                new NoOpMouseAction());
	}
}
