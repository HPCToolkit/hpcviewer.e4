package edu.rice.cs.hpctree.internal.config;

import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;

import edu.rice.cs.hpcsetting.color.ColorManager;


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

		// Fix issue #132 more contrast is needed for color selection
		defaultFgColor = ColorManager.getTextFg(this.defaultBgColor);
        defaultSelectionFgColor = ColorManager.getTextFg(this.defaultSelectionBgColor);

        selectionAnchorFgColor  = defaultSelectionFgColor;
        selectionAnchorBgColor  = defaultSelectionBgColor;
		
        selectionAnchorSelectionBgColor = defaultSelectionBgColor;
        selectionAnchorSelectionFgColor = defaultSelectionFgColor;
        
		cHeaderSelectionBgColor = GUIHelper.COLOR_DARK_GRAY;
		cHeaderSelectionFgColor = ColorManager.getTextFg(cHeaderSelectionBgColor);
	}
	
	
	
    @Override
    public void createPainterInstances() {
    	super.createPainterInstances();
    	
        treeStructurePainter = ScopeTreePainter.getTreeStructurePainter();
        treeStructureSelectionPainter = ScopeTreePainter.getTreeStructureSelectionPainter();
        selectedSortHeaderCellPainter = ScopeTreePainter.getSelectedSortHeaderCellPainter();
    }
}
