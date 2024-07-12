// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandToLevelCommand;

import edu.rice.cs.hpctree.ScopeTreeLayer;

public class ScopeTreeExpandToLevelCommandHandler extends AbstractLayerCommandHandler<TreeExpandToLevelCommand> 
{
	private ScopeTreeLayer treeLayer;
	
	public ScopeTreeExpandToLevelCommandHandler(ScopeTreeLayer treeLayer) {
		this.treeLayer = treeLayer;
	}

	@Override
	public Class<TreeExpandToLevelCommand> getCommandClass() {
        return TreeExpandToLevelCommand.class;
	}

	@Override
	protected boolean doCommand(TreeExpandToLevelCommand command) {
		int parent = command.getParentIndex();
		treeLayer.expandTreeRow(parent);
		return true;
	}

}
