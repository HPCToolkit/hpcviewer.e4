// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcfilter.cct;

import org.eclipse.jface.dialogs.IInputValidator;

public class PatternValidator implements IInputValidator {

	@Override
	public String isValid(String newText) {

		if (newText == null || newText.length() < 1) {
			return "Pattern cannot be empty.";
		}
		return null;
	}

}
