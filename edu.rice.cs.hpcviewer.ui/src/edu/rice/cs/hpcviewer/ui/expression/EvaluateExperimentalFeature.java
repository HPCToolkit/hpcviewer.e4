// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcviewer.ui.expression;

import org.eclipse.e4.core.di.annotations.Evaluate;

import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;

public class EvaluateExperimentalFeature {
	@Evaluate
	public boolean evaluate() {
		return ViewerPreferenceManager.INSTANCE.getExperimentalFeatureMode();
	}
}
