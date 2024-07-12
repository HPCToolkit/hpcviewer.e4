// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcfilter.internal;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Image;

public class CheckBoxConfiguration extends AbstractRegistryConfiguration 
{
	private final String configLable;
	
	public CheckBoxConfiguration(String configLable) {
		this.configLable = configLable;
	}
	
	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, 
											   IEditableRule.ALWAYS_EDITABLE,
											   DisplayMode.NORMAL,
											   configLable);

		// need to download better resolution images
		// the original checkbox images are 16x16, 
		// and it doesn't look great on mac
		//
		Image checked = GUIHelper.getImage("checked_120_120", true, true);
		Image uncheck = GUIHelper.getImage("unchecked_120_120", true, true);
		CheckBoxPainter checkboxPainter = new CheckBoxPainter(checked, uncheck, true); 
		
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, 
											   checkboxPainter, 
											   DisplayMode.NORMAL, 
											   configLable);
		
		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, 
											   new DefaultBooleanDisplayConverter(), 
											   DisplayMode.NORMAL, 
											   configLable);
		
		configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, 
											   new CheckBoxCellEditor(), 
											   DisplayMode.EDIT,
											   configLable);
	}

}
