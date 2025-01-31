// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctree.internal.config;

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

		treeStructurePainter = ScopeTreePainter.getTreeStructurePainter(dataProvider, defaultBgColor);    	
        treeStructureSelectionPainter = treeStructurePainter;
        selectedSortHeaderCellPainter = ScopeTreePainter.getSelectedSortHeaderCellPainter();
    }
}
