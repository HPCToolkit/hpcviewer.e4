package edu.rice.cs.hpctree.internal.config;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.style.theme.DefaultNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.tree.painter.IndentedTreeImagePainter;
import org.eclipse.nebula.widgets.nattable.tree.painter.TreeImagePainter;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;

import edu.rice.cs.hpcsetting.fonts.FontManager;

public class ScopeTableStyleConfiguration extends DefaultNatTableThemeConfiguration 
{
    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {    	
    	super.configureRegistry(configRegistry);
    	cHeaderFont = FontManager.getFontGeneric();
    	cHeaderSelectionFont = FontManager.getFontGeneric();  
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
}
