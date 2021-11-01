package edu.rice.cs.hpctree.internal.config;

import org.eclipse.nebula.widgets.nattable.style.theme.DarkNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;

public class DarkScopeTableStyleConfiguration extends DarkNatTableThemeConfiguration 
{
	
	public DarkScopeTableStyleConfiguration() {
		super();
		// Fix issue #120: the color has to make the line separator visible
		// so far dark gray is good enough, for most themes (to be verified)
		cHeaderBgColor = GUIHelper.COLOR_WHITE;
		cHeaderFgColor = GUIHelper.COLOR_DARK_GRAY;
		
		cHeaderSelectionBgColor = GUIHelper.COLOR_WHITE;
		cHeaderSelectionFgColor = GUIHelper.COLOR_DARK_GRAY;
		
        selectionAnchorSelectionBgColor = defaultSelectionBgColor;
        selectionAnchorSelectionFgColor = defaultSelectionFgColor;

	}
	
	
    @Override
    public void createPainterInstances() {
    	super.createPainterInstances();
    	
        treeStructurePainter = ScopeTreePainter.getTreeStructureSelectionPainter();
        treeStructureSelectionPainter = ScopeTreePainter.getTreeStructureSelectionPainter();
        selectedSortHeaderCellPainter = ScopeTreePainter.getSelectedSortHeaderCellPainter();
    }
}
