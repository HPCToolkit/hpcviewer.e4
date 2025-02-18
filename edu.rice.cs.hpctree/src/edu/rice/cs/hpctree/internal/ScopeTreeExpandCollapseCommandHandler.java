// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.tree.command.TreeExpandCollapseCommand;

import edu.rice.cs.hpctree.ScopeTreeLayer;

public class ScopeTreeExpandCollapseCommandHandler extends AbstractLayerCommandHandler<TreeExpandCollapseCommand> 
{
	private final ScopeTreeLayer treeLayer;
	
	public ScopeTreeExpandCollapseCommandHandler(ScopeTreeLayer treeLayer) {
		this.treeLayer = treeLayer;
	}

	@Override
	public Class<TreeExpandCollapseCommand> getCommandClass() {
		return TreeExpandCollapseCommand.class;
	}

	@Override
	protected boolean doCommand(TreeExpandCollapseCommand command) {
        int parentIndex = command.getParentIndex();
        treeLayer.expandOrCollapseIndex(parentIndex);
		return false;
	}

}
