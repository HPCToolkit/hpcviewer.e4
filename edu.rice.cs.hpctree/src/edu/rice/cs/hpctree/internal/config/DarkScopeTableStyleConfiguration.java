package edu.rice.cs.hpctree.internal.config;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import edu.rice.cs.hpcsetting.table.DarkThemeConfiguration;
import edu.rice.cs.hpctree.internal.ScopeTreeDataProvider;

public class DarkScopeTableStyleConfiguration extends DarkThemeConfiguration 
{	
	private ScopeTreeDataProvider dataProvider;
	private final Color separatorColor;
	
    public DarkScopeTableStyleConfiguration(NatTable nattable, ScopeTreeDataProvider dataProvider) {
		super(nattable);
		this.dataProvider = dataProvider;
		separatorColor = nattable.getDisplay().getSystemColor(SWT.COLOR_DARK_CYAN);
	}

	@Override
    public void createPainterInstances() {
    	super.createPainterInstances();

		treeStructurePainter = ScopeTreePainter.getTreeStructurePainter(dataProvider, defaultBgColor);    	
        treeStructureSelectionPainter = treeStructurePainter;
        selectedSortHeaderCellPainter = ScopeTreePainter.getSelectedSortHeaderCellPainter();
    }
	
	
	@Override
	protected Color getFreezeSeparatorColor() {
		// issue #134: make separator more contrast in the dark mode
		return separatorColor;
	}
	
	protected Integer getFreezeSeparatorWidth() {
		return 2;
	}
}
