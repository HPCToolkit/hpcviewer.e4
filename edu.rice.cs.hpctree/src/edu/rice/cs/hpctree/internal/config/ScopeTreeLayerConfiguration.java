// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctree.internal.config;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.tree.action.TreeExpandCollapseAction;
import org.eclipse.nebula.widgets.nattable.tree.painter.TreeImagePainter;
import org.eclipse.nebula.widgets.nattable.ui.action.NoOpMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellPainterMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;

public class ScopeTreeLayerConfiguration implements IConfiguration 
{
	@Override
	public void configureLayer(ILayer layer) {
	}

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
	}

	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        TreeExpandCollapseAction treeExpandCollapseAction = new TreeExpandCollapseAction();
        
        CellPainterMouseEventMatcher treeImagePainterMouseEventMatcher =
                new CellPainterMouseEventMatcher(
                        GridRegion.BODY,
                        MouseEventMatcher.LEFT_BUTTON,
                        TreeImagePainter.class);

        uiBindingRegistry.registerFirstSingleClickBinding(
                treeImagePainterMouseEventMatcher,
                treeExpandCollapseAction);

        // Obscure any mouse down bindings for this image painter
        uiBindingRegistry.registerFirstMouseDownBinding(
                treeImagePainterMouseEventMatcher,
                new NoOpMouseAction());
	}
}
