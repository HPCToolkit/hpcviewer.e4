 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcviewer.ui.preferences.ViewerPreferenceManager;

public class ShowPreferences 
{
	@Execute
	public void execute(@Active Shell shell) {
		new ViewerPreferenceManager().run(shell);;
	}
		
}