package edu.rice.cs.hpctree.internal.config;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.VisualRefreshCommand;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;

import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;

public class HeaderLayerConfiguration implements IConfiguration, IConfigLabelAccumulator, IPropertyChangeListener 
{
	private final static String LABEL_COLUMN = "col";
	
	private final NatTable table;
	
	public HeaderLayerConfiguration(NatTable table) {
		this.table = table;
		PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		pref.addPropertyChangeListener(this);
	}

	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {}
	
	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {
		final Style style = new Style();
		style.setAttributeValue(CellStyleAttributes.FONT, TableFontConfiguration.getGenericFont());

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.SELECT, LABEL_COLUMN);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL, LABEL_COLUMN);
	}
	
	@Override
	public void configureLayer(ILayer layer) {}

	
	@Override
	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		configLabels.add(LABEL_COLUMN);
	}


	@Override
	public void propertyChange(PropertyChangeEvent event) {

		final String property = event.getProperty();
		
		boolean need_to_refresh = (property.equals(PreferenceConstants.ID_FONT_GENERIC) || 
								   property.equals(PreferenceConstants.ID_FONT_METRIC)); 
		
		if (need_to_refresh) {
			IConfigRegistry registry = table.getConfigRegistry();
			configureRegistry(registry);
			table.doCommand(new VisualRefreshCommand());
		}
	}
}
