 
package edu.rice.cs.hpcviewer.ui.expression;

import org.eclipse.e4.core.di.annotations.Evaluate;

import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;

public class DebugMode 
{
	@Evaluate
	public boolean evaluate() {
		return ViewerPreferenceManager.INSTANCE.getDebugMode();
	}
}
