// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctree.internal.config;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;
import edu.rice.cs.hpctree.ScopeTreeTable;
import edu.rice.cs.hpctree.internal.ScopeTreeLabelAccumulator;

/***************************************************
 * 
 * Configuration to manage the appearance of the table:
 * Fonts, colors and glyphs.
 * <p>
 * This class takes into account the setting change in the hpcsetting
 * (preference window).
 *
 ***************************************************/
public class TableFontConfiguration implements IConfiguration, IPropertyChangeListener, DisposeListener
{
	private final ScopeTreeTable treeTable;
	
	public TableFontConfiguration(ScopeTreeTable natTable) {
		this.treeTable = natTable;
		
		PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		pref.addPropertyChangeListener(this);
		
		this.treeTable.getTable().addDisposeListener(this);
	}
	
	
	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		configureFont(configRegistry);
	}


	@Override
	public void propertyChange(PropertyChangeEvent event) {

		final String property = event.getProperty();
		
		boolean need_to_refresh = (property.equals(PreferenceConstants.ID_FONT_GENERIC)  || 
								   property.equals(PreferenceConstants.ID_FONT_METRIC)   ||
								   property.equals(PreferenceConstants.ID_FONT_CALLSITE) ||
								   property.equals(PreferenceConstants.ID_CHAR_CALLFROM) ||
								   property.equals(PreferenceConstants.ID_CHAR_CALLTO)); 
		
		if (need_to_refresh) {
			configureFont(treeTable.getTable().getConfigRegistry());
			treeTable.visualRefresh();
			treeTable.pack(true);
		}
	}
	
	
	/****
	 * Public method to configure the header font. <br/>
	 * Due to NatTable bug, we have to configure the header font once the table is created,
	 * otherwise we don't see any change in the font header.
	 * @see hpcviewer bug issue #204 
	 *  
	 * @param configRegistry
	 * 			The table configuration registry
	 */
	public void configureHeaderFont(IConfigRegistry configRegistry) {
    	
		// set the default to the generic font
		IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
		if (style == null) {
			style = new Style();
		}
		style.setAttributeValue(CellStyleAttributes.FONT, FontManager.getFontGeneric());

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.SELECT, GridRegion.COLUMN_HEADER);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);

	}
	
	protected void configureFont(IConfigRegistry configRegistry) {
		configureHeaderFont(configRegistry);
		
		// set the font for tree column
		final Style styleTree  = new Style();
		styleTree.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
		styleTree.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
		styleTree.setAttributeValue(CellStyleAttributes.FONT, FontManager.getFontGeneric());

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, styleTree, DisplayMode.SELECT, ScopeTreeLabelAccumulator.LABEL_TREECOLUMN);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, styleTree, DisplayMode.NORMAL, ScopeTreeLabelAccumulator.LABEL_TREECOLUMN);

		// set the font for metric columns
		final Style styleMetric = new Style();
		styleMetric.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
		styleMetric.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
		styleMetric.setAttributeValue(CellStyleAttributes.FONT, FontManager.getMetricFont());

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, styleMetric, DisplayMode.SELECT, ScopeTreeLabelAccumulator.LABEL_METRICOLUMN);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, styleMetric, DisplayMode.NORMAL, ScopeTreeLabelAccumulator.LABEL_METRICOLUMN);
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {
		PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		pref.removePropertyChangeListener(this);		
	}

	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) { /* not needed */ }

	@Override
	public void configureLayer(ILayer layer) { /* not needed */ }
}
