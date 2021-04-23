 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.widgets.Shell;

/*****************************************************
 * 
 * Class handler to minimize window programmatically
 *
 *****************************************************/
public class WindowMinimize 
{
	@Execute
	public void execute(Shell shell) {
		// Fix issue #85 (minimize window)
		shell.setMinimized(true);
	}
		
}