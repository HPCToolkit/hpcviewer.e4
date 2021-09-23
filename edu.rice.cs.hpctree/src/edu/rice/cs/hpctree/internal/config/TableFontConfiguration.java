package edu.rice.cs.hpctree.internal.config;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.swt.graphics.Font;

import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;
import edu.rice.cs.hpctree.ScopeTreeTable;
import edu.rice.cs.hpctree.internal.ScopeTreeLabelAccumulator;

public class TableFontConfiguration implements IConfiguration, IPropertyChangeListener 
{
	private IConfigRegistry configRegistry;
	private final ScopeTreeTable treeTable;
	
	public TableFontConfiguration(ScopeTreeTable treeTable) {
		this.treeTable = treeTable;
	}
	
	
	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {

		this.configRegistry = configRegistry;
		
		// configuration for tree column
		//
		final Font fontGeneric = getGenericFont();
		final Style styleTree  = new Style();
		
		styleTree.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
		styleTree.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
		styleTree.setAttributeValue(CellStyleAttributes.FONT, fontGeneric);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
											   styleTree, 
											   DisplayMode.NORMAL, 
											   ScopeTreeLabelAccumulator.LABEL_TREECOLUMN);
		
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
				   							   styleTree, 
											   DisplayMode.SELECT, 
											   ScopeTreeLabelAccumulator.LABEL_TREECOLUMN);

		// configuration for metric column
		//
		final Font fontMetric  = getMetricFont();
		final Style styleMetric = new Style();

		styleMetric.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
		styleMetric.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);
		styleMetric.setAttributeValue(CellStyleAttributes.FONT, fontMetric);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
				   styleMetric, 
				   DisplayMode.NORMAL, 
				   ScopeTreeLabelAccumulator.LABEL_METRICOLUMN);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
				   styleMetric, 
				   DisplayMode.SELECT, 
				   ScopeTreeLabelAccumulator.LABEL_METRICOLUMN);

		
		PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		pref.addPropertyChangeListener(this);
	}


	@Override
	public void propertyChange(PropertyChangeEvent event) {

		final String property = event.getProperty();
		
		boolean need_to_refresh = (property.equals(PreferenceConstants.ID_FONT_GENERIC) || 
								   property.equals(PreferenceConstants.ID_FONT_METRIC)  ||
								   property.equals(PreferenceConstants.ID_DEBUG_CCT_ID) ||
								   property.equals(PreferenceConstants.ID_DEBUG_FLAT_ID) ); 
		
		if (need_to_refresh) {
			configureRegistry(configRegistry);
			treeTable.attributeRefresh();
		}
	}

	public static Font getMetricFont() {
		Font font ;
		try {
			font = FontManager.getMetricFont();
		} catch (Exception e) {
			font = JFaceResources.getTextFont();
		}
		return font;
	}

	public static Font getGenericFont() {
		Font font;
		try {
			font = FontManager.getFontGeneric();
		} catch (Exception e) {
			font = JFaceResources.getDefaultFont();
		}
		return font;
	}


	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {}

	@Override
	public void configureLayer(ILayer layer) {}
}
