package edu.rice.cs.hpctree.internal.config;

import org.eclipse.swt.graphics.Color;

import edu.rice.cs.hpcsetting.color.ColorManager;
import edu.rice.cs.hpcsetting.table.DayThemeConfiguration;
import edu.rice.cs.hpctree.internal.ScopeTreeDataProvider;


/*****************************************************************************
 * 
 * Set the standard style for the hpcviewer table.
 * Instantiating this class requires the caller to call {@code addDisposeListener} method
 * to this instance to free some resources and listeners.
 *
 *****************************************************************************/
public class ScopeTableStyleConfiguration extends DayThemeConfiguration 
{
	private ScopeTreeDataProvider dataProvider;
	
	public ScopeTableStyleConfiguration(ScopeTreeDataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}
		
	
    @Override
    public void createPainterInstances() {
    	super.createPainterInstances();

    	Color fg = ColorManager.getTextFg(defaultBgColor);
    	if (fg == ColorManager.COLOR_WHITE)
    		treeStructurePainter = ScopeTreePainter.getInvTreeStructurePainter(dataProvider);
    	else 
    		treeStructurePainter = ScopeTreePainter.getTreeStructurePainter(dataProvider);

        treeStructureSelectionPainter = treeStructurePainter;
        selectedSortHeaderCellPainter = ScopeTreePainter.getSelectedSortHeaderCellPainter();
    }
}
