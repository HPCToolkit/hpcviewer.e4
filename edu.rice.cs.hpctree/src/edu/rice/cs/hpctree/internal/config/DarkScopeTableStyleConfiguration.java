package edu.rice.cs.hpctree.internal.config;

import org.eclipse.nebula.widgets.nattable.NatTable;
import edu.rice.cs.hpcsetting.table.DarkThemeConfiguration;

public class DarkScopeTableStyleConfiguration extends DarkThemeConfiguration 
{	
	
	
    public DarkScopeTableStyleConfiguration(NatTable nattable) {
		super(nattable);
	}

	@Override
    public void createPainterInstances() {
    	super.createPainterInstances();
    	
        treeStructurePainter = ScopeTreePainter.getTreeStructureSelectionPainter();
        treeStructureSelectionPainter = ScopeTreePainter.getTreeStructureSelectionPainter();
        selectedSortHeaderCellPainter = ScopeTreePainter.getSelectedSortHeaderCellPainter();
    }
}
