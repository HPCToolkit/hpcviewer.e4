package edu.rice.cs.hpctree.internal.config;

import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;


/*****************************************************************************
 * 
 * Set the standard style for the hpcviewer table.
 * Instantiating this class requires the caller to call {@code addDisposeListener} method
 * to this instance to free some resources and listeners.
 *
 *****************************************************************************/
public class ScopeTableStyleConfiguration extends ModernNatTableThemeConfiguration 
{
	public ScopeTableStyleConfiguration() {
		super();
		// Fix issue #120: the color has to make the line separator visible
		// so far dark gray is good enough, for most themes (to be verified)
		cHeaderBgColor = GUIHelper.COLOR_DARK_GRAY;
		cHeaderFgColor = GUIHelper.COLOR_WHITE;
		
		cHeaderSelectionBgColor = GUIHelper.COLOR_DARK_GRAY;
		cHeaderSelectionFgColor = GUIHelper.COLOR_WHITE;
	}
	
	
	
    @Override
    public void createPainterInstances() {
    	super.createPainterInstances();
    	
        treeStructurePainter = ScopeTreePainter.getTreeStructurePainter();
        treeStructureSelectionPainter = ScopeTreePainter.getTreeStructureSelectionPainter();
        selectedSortHeaderCellPainter = ScopeTreePainter.getSelectedSortHeaderCellPainter();
    }
}
