 
package edu.rice.cs.hpcviewer.ui.expression;

import org.eclipse.e4.core.di.annotations.Evaluate;

import edu.rice.cs.hpcviewer.ui.preferences.ViewerPreferenceManager;

public class DebugMode 
{
	@Evaluate
	public boolean evaluate() {
		ViewerPreferenceManager pref = ViewerPreferenceManager.INSTANCE;
		return pref.getDebugMode();
	}
}
