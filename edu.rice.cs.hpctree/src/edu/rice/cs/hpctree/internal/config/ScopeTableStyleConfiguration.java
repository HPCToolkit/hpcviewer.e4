package edu.rice.cs.hpctree.internal.config;

import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;


/*****************************************************************************
 * 
 * Set the standard style for the hpcviewer table.
 * Instantiating this class requires the caller to call {@code addDisposeListener} method
 * to this instance to free some resources and listeners.
 *
 *****************************************************************************/
public class ScopeTableStyleConfiguration extends ModernNatTableThemeConfiguration 
{       
    @Override
    public void createPainterInstances() {
    	super.createPainterInstances();
    	
        treeStructurePainter = ScopeTreePainter.getTreeStructurePainter();
        treeStructureSelectionPainter = ScopeTreePainter.getTreeStructureSelectionPainter();
        selectedSortHeaderCellPainter = ScopeTreePainter.getSelectedSortHeaderCellPainter();
    }
}
