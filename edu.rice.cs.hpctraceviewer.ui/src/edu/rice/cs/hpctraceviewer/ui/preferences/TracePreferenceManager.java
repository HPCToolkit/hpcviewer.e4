package edu.rice.cs.hpctraceviewer.ui.preferences;

import org.eclipse.jface.preference.PreferenceStore;

import edu.rice.cs.hpcsetting.preferences.AbstractPreferenceManager;

public class TracePreferenceManager extends AbstractPreferenceManager
{
	public static final TracePreferenceManager INSTANCE = new TracePreferenceManager();
	
	public TracePreferenceManager() {
	}

	@Override
	public void setDefaults() {
		PreferenceStore store = getPreferenceStore();

		store.setDefault(TracePreferenceConstants.PREF_RENDER_OPTION, TracePreferenceConstants.RENDERING_MIDPOINT);
		store.setDefault(TracePreferenceConstants.PREF_TOOLTIP_DELAY, TracePreferenceConstants.TOOLTIP_DELAY_DEFAULT);
	}

}
