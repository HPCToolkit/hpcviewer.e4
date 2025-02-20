// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.config;

import java.io.IOException;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcdata.util.ThreadManager;
import edu.rice.cs.hpcsetting.preferences.AbstractPage;

/********************************************************
 * 
 * Main preference page for trace view part
 *
 ********************************************************/
public class TracePreferencePage extends AbstractPage 
{
	public static final String TITLE = "Traces";
	
	private static final int TOOLTIP_DELAY_MAX_MS = 10000;
	private static final int TOOLTIP_DELAY_INCREMENT_MS = 1000;
	
	private Button []colorPolicies;
	private Button gpuTrace;
	
	private Spinner tooltipDelay;
	private Spinner spMaxThreads;

	public TracePreferencePage() {
		super(TITLE);
	}

	
	@Override
	public boolean performOk() {
		apply();
		return true;
	}

	@Override
	public void apply() {
		if (colorPolicies == null || tooltipDelay == null) 
			return;
		
		PreferenceStore pref = TracePreferenceManager.INSTANCE.getPreferenceStore();	
		
		int colorOld = pref.getInt(TracePreferenceConstants.PREF_COLOR_OPTION);		
		for (int i=0; i<colorPolicies.length; i++) {
			Button btn = colorPolicies[i];
			boolean isSelected = btn.getSelection();
			if (isSelected && (i != colorOld)) {
				pref.setValue(TracePreferenceConstants.PREF_COLOR_OPTION, i);
				break;
			}
		}
		
		boolean isGPU = gpuTrace.getSelection();
		pref.setValue(TracePreferenceConstants.PREF_GPU_TRACES, isGPU);
		
		int maxThreads = spMaxThreads.getSelection();
		pref.setValue(TracePreferenceConstants.PREF_MAX_THREADS, maxThreads);
		
		int delay = tooltipDelay.getSelection();
		pref.setValue(TracePreferenceConstants.PREF_TOOLTIP_DELAY, delay);
		
		try {
			pref.save();
		} catch (IOException e) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Unable to save the settings", e);
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		
		TracePreferenceManager.INSTANCE.setDefaults();

		// ------------------------------------------------------------------------
		// Color generator
		// ------------------------------------------------------------------------

		Composite groupColor = createGroupControl(parent, "Color policy", false);
		groupColor.setToolTipText("Change the policy to map the procedure's name to a color.\n" +
								  "The change will take effect at the next session");

		colorPolicies = createRadioButtonControl(groupColor, TracePreferenceConstants.colorOptions);
		Label lbl = new Label(groupColor, SWT.NONE);
		lbl.setText("The change will take effect at the next session");
		
		PreferenceStore pref = TracePreferenceManager.INSTANCE.getPreferenceStore();		
		int colorSelected    = pref.getInt(TracePreferenceConstants.PREF_COLOR_OPTION);
		colorPolicies[colorSelected].setSelection(true);
		
		// ------------------------------------------------------------------------
		// GPU traces
		// ------------------------------------------------------------------------
		
		Composite groupGPU = createGroupControl(parent, "GPU Traces", false);
		groupGPU.setToolTipText("Configure how the GPU traces will be rendered");
		
		gpuTrace = createCheckBoxControl(groupGPU, "Expose GPU traces");
		boolean isGPU = pref.getBoolean(TracePreferenceConstants.PREF_GPU_TRACES);
		gpuTrace.setSelection(isGPU);
		
		createLabelControl(groupGPU, "This will reveal GPU traces whenever possible. Enabling this option causes trace stratistics may not be reliable.");

		// ------------------------------------------------------------------------
		// maximum threads
		// ------------------------------------------------------------------------
		
		Composite groupThreads = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(groupThreads);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(groupThreads);
		
		int maxThreads = pref.getInt(TracePreferenceConstants.PREF_MAX_THREADS);
		
		createLabelControl(groupThreads, "Max number of painting threads: ");
		spMaxThreads = createSpinnerControl(groupThreads, 0, ThreadManager.getNumThreads(0));
		spMaxThreads.setPageIncrement(1);
		spMaxThreads.setToolTipText("Maximum number of threads to paint the traces.");
		spMaxThreads.setSelection( Math.min(ThreadManager.getNumThreads(0), maxThreads) );

		// ------------------------------------------------------------------------
		// tooltip delay
		// ------------------------------------------------------------------------
		
		Composite group = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(group);
		
		int tooltipDelayValue = pref.getInt(TracePreferenceConstants.PREF_TOOLTIP_DELAY);
		
		createLabelControl(group, "Tooltip's delay in trace views (in miliseconds)");
		tooltipDelay = createSpinnerControl(group, 0, TOOLTIP_DELAY_MAX_MS);
		tooltipDelay.setPageIncrement(TOOLTIP_DELAY_INCREMENT_MS);
		tooltipDelay.setToolTipText("Set the delay of the tooltip appearence (in ms) on top of the system's delay");
		tooltipDelay.setSelection(tooltipDelayValue);
		
		return null;
	}

	
	
	@Override
	public IPreferenceStore getPreferenceStore() {
		return TracePreferenceManager.INSTANCE.getPreferenceStore();
	}
	
	@Override
	protected void performDefaults() {

		colorPolicies[TracePreferenceConstants.COLOR_NAME_BASED].setSelection(true);
		colorPolicies[TracePreferenceConstants.COLOR_RANDOM].setSelection(false);
		
		int maxThreads = Math.min(ThreadManager.getNumThreads(0), TracePreferenceConstants.DEFAULT_MAX_THREADS);
		spMaxThreads.setSelection(maxThreads);
		
		tooltipDelay.setSelection(TracePreferenceConstants.DEFAULT_TOOLTIP_DELAY);
		
		TracePreferenceManager.INSTANCE.setDefaults();
		
		super.performDefaults();
	}
}
