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
		store.setDefault(TracePreferenceConstants.PREF_TOOLTIP_DELAY, TracePreferenceConstants.DEFAULT_TOOLTIP_DELAY);
		store.setDefault(TracePreferenceConstants.PREF_MAX_THREADS,   TracePreferenceConstants.DEFAULT_MAX_THREADS);
	}
	
	
	/****
	 * check if midpoint painting is enabled
	 * @return true if midpoint painting is enabled. False otherwise.
	 */
	public static boolean isMidpointEnabled() {
		int renderOption = getRenderOption();
		return renderOption == TracePreferenceConstants.RENDERING_MIDPOINT;
	}
	
	private static int getRenderOption() {
		return INSTANCE.getPreferenceStore().getInt(TracePreferenceConstants.PREF_RENDER_OPTION);
	}
	
	public static int getMaxThreads() {
		return INSTANCE.getPreferenceStore().getInt(TracePreferenceConstants.PREF_MAX_THREADS);
	}
}
