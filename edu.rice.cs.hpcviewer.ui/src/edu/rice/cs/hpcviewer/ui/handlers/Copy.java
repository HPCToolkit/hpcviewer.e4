 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;

import edu.rice.cs.hpcviewer.ui.parts.IBasePart;
import edu.rice.cs.hpcviewer.ui.parts.IViewPart;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;

public class Copy {
	@Execute
	public void execute() {
		
	}
	
	
	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_PART) MPart part, @Optional @Named(IServiceConstants.ACTIVE_SELECTION) Object s ) {
		if (part == null) return false;
		if (!(part instanceof IBasePart)) return false;
		
		if (part instanceof IViewPart) {
			
		}
		
		return true;
	}
		
}