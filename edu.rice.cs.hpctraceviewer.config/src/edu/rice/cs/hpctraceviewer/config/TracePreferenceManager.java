// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.config;

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

		store.setDefault(TracePreferenceConstants.PREF_COLOR_OPTION,  TracePreferenceConstants.COLOR_NAME_BASED);
		store.setDefault(TracePreferenceConstants.PREF_RENDER_OPTION, TracePreferenceConstants.RENDERING_RIGHTMOST);
		store.setDefault(TracePreferenceConstants.PREF_TOOLTIP_DELAY, TracePreferenceConstants.DEFAULT_TOOLTIP_DELAY);
		store.setDefault(TracePreferenceConstants.PREF_MAX_THREADS,   TracePreferenceConstants.DEFAULT_MAX_THREADS);
		store.setDefault(TracePreferenceConstants.PREF_GPU_TRACES,    TracePreferenceConstants.DEFAULT_GPU_TRACES);
	}
	
	
	/****
	 * check if midpoint painting is enabled
	 * @return true if midpoint painting is enabled. False otherwise.
	 */
	public static boolean isMidpointEnabled() {
		return false;
		/*
		int renderOption = getRenderOption();
		return renderOption == TracePreferenceConstants.RENDERING_MIDPOINT;
		*/
	}
	
	
	/*****
	 * Check if the name-based color creation policy is enabled
	 * @return true if name-based color policy has to be used.
	 */
	public static boolean useNameBasedColorPolicy() {
		int colorOption = getColorOption();
		return colorOption == TracePreferenceConstants.COLOR_NAME_BASED;
	}
	
	/***
	 * Get the maximum threads can be used to paint the canvas
	 * @return
	 */
	public static int getMaxThreads() {
		return INSTANCE.getPreferenceStore().getInt(TracePreferenceConstants.PREF_MAX_THREADS);
	}
	
	public static int getTooltipDelay() {
		return INSTANCE.getPreferenceStore().getInt(TracePreferenceConstants.PREF_TOOLTIP_DELAY);
	}

	public static boolean getGPUTraceExposure() {
		return INSTANCE.getPreferenceStore().getBoolean(TracePreferenceConstants.PREF_GPU_TRACES);
	}
	
	////////////////////////////////////////
	// 
	// Private methods
	//
	////////////////////////////////////////

	/*
	private static int getRenderOption() {
		return INSTANCE.getPreferenceStore().getInt(TracePreferenceConstants.PREF_RENDER_OPTION);
	} */
	
	private static int getColorOption() {
		return INSTANCE.getPreferenceStore().getInt(TracePreferenceConstants.PREF_COLOR_OPTION);
	}
}
