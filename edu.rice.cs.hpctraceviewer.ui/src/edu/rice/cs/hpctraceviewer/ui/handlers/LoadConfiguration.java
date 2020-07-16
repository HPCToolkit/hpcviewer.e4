 
package edu.rice.cs.hpctraceviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.CanExecute;

public class LoadConfiguration {
	@Execute
	public void execute() {
		
	}
	
	
	@CanExecute
	public boolean canExecute() {
		
		return true;
	}
		
}