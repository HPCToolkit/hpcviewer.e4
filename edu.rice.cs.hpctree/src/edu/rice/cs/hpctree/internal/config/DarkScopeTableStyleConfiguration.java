package edu.rice.cs.hpctree.internal.config;

import org.eclipse.nebula.widgets.nattable.style.theme.DarkNatTableThemeConfiguration;

public class DarkScopeTableStyleConfiguration extends DarkNatTableThemeConfiguration 
{
    @Override
    public void createPainterInstances() {
    	super.createPainterInstances();
    	
        treeStructurePainter = ScopeTreePainter.getTreeStructureSelectionPainter();
        treeStructureSelectionPainter = ScopeTreePainter.getTreeStructurePainter();
        selectedSortHeaderCellPainter = ScopeTreePainter.getSelectedSortHeaderCellPainter();
    }
}
