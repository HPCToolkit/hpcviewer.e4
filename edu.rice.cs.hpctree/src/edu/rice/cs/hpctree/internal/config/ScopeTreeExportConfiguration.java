// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctree.internal.config;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.export.ExportConfigAttributes;
import org.eclipse.nebula.widgets.nattable.export.action.ExportAction;
import org.eclipse.nebula.widgets.nattable.export.csv.CsvExporter;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.tree.config.TreeExportFormatter;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.swt.SWT;

import edu.rice.cs.hpctree.ScopeTreeRowModel;

public class ScopeTreeExportConfiguration implements IConfiguration 
{
	private ScopeTreeRowModel treeModel;
	
	public ScopeTreeExportConfiguration(ScopeTreeRowModel treeModel) {
		this.treeModel = treeModel;
	}
	
	@Override
	public void configureLayer(ILayer layer) {}

	
	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(
                ExportConfigAttributes.EXPORTER,
                new CsvExporter());
        configRegistry.registerConfigAttribute(
                ExportConfigAttributes.EXPORT_FORMATTER,
                new TreeExportFormatter(treeModel));
        configRegistry.registerConfigAttribute(
                ExportConfigAttributes.DATE_FORMAT, "m/d/yy h:mm"); //$NON-NLS-1$
	}

	
	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
        uiBindingRegistry.registerKeyBinding(
                new KeyEventMatcher(SWT.MOD1, 'e'),
                new ExportAction());
	}

}
