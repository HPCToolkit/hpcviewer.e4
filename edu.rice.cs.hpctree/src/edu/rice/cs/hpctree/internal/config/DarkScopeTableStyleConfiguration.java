package edu.rice.cs.hpctree.internal.config;

import org.eclipse.nebula.widgets.nattable.NatTable;
import edu.rice.cs.hpcsetting.table.DarkThemeConfiguration;
import edu.rice.cs.hpctree.internal.ScopeTreeDataProvider;

public class DarkScopeTableStyleConfiguration extends DarkThemeConfiguration 
{	
	private ScopeTreeDataProvider dataProvider;
	
    public DarkScopeTableStyleConfiguration(NatTable nattable, ScopeTreeDataProvider dataProvider) {
		super(nattable);
		this.dataProvider = dataProvider;
	}

	@Override
    public void createPainterInstances() {
    	super.createPainterInstances();

		treeStructurePainter = ScopeTreePainter.getTreeStructurePainter(dataProvider, defaultBgColor);    	
        treeStructureSelectionPainter = treeStructurePainter;
        selectedSortHeaderCellPainter = ScopeTreePainter.getSelectedSortHeaderCellPainter();
    }
}
