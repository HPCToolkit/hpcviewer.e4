package edu.rice.cs.hpctree.internal.config;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.VisualRefreshCommand;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.tree.painter.IndentedTreeImagePainter;
import org.eclipse.nebula.widgets.nattable.tree.painter.TreeImagePainter;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;

import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;

public class ScopeTableStyleConfiguration extends ModernNatTableThemeConfiguration implements IPropertyChangeListener
{
	private final NatTable table;

	public ScopeTableStyleConfiguration(NatTable table) {
		this.table = table;
	}
	
	
	@Override
    public void configureRegistry(IConfigRegistry configRegistry) {    	
    	super.configureRegistry(configRegistry);
		IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
		if (style == null) {
			style = new Style();
		}
		style.setAttributeValue(CellStyleAttributes.FONT, TableFontConfiguration.getGenericFont());

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.SELECT, GridRegion.COLUMN_HEADER);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
    }
    
    
    @Override
    public void createPainterInstances() {
    	super.createPainterInstances();
    	
        TreeImagePainter treeImagePainter =
                new TreeImagePainter(
                        false,
                        GUIHelper.getImage("right_144_144"), //$NON-NLS-1$
                        GUIHelper.getImage("right_down_144_144"), //$NON-NLS-1$
                        null);
        treeStructurePainter =
                new BackgroundPainter(
                        new PaddingDecorator(
                                new IndentedTreeImagePainter(
                                        10,
                                        null,
                                        CellEdgeEnum.LEFT,
                                        treeImagePainter,
                                        false,
                                        2,
                                        true),
                                0, 5, 0, 5, false));

        TreeImagePainter treeSelectionImagePainter =
                new TreeImagePainter(
                        false,
                        GUIHelper.getImage("right_inv_144_144"), //$NON-NLS-1$
                        GUIHelper.getImage("right_down_inv_144_144"), //$NON-NLS-1$
                        null);
        treeStructureSelectionPainter =
                new BackgroundPainter(
                        new PaddingDecorator(
                                new IndentedTreeImagePainter(
                                        10,
                                        null,
                                        CellEdgeEnum.LEFT,
                                        treeSelectionImagePainter,
                                        false,
                                        2,
                                        true),
                                0, 5, 0, 5, false));
    }

	@Override
	public void propertyChange(PropertyChangeEvent event) {

		final String property = event.getProperty();
		
		boolean need_to_refresh = (property.equals(PreferenceConstants.ID_FONT_GENERIC) || 
								   property.equals(PreferenceConstants.ID_FONT_METRIC)); 
		
		if (need_to_refresh) {
			IConfigRegistry registry = table.getConfigRegistry();
			configureRegistry(registry);
			table.doCommand(new VisualRefreshCommand());
		}
	}
}
