package edu.rice.cs.hpc.filter.pattern;

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
