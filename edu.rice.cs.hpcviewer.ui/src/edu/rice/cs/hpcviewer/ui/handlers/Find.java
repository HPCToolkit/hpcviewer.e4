 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcviewer.ui.dialogs.FindDialog;
import edu.rice.cs.hpcviewer.ui.parts.editor.Editor;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;


/****************************************************************
 * 
 * Handler to find a text in the current active editor part
 *
 ****************************************************************/
public class Find 
{
	private FindDialog findDialog = null;
	
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_PART) MPart part,
						@Named(IServiceConstants.ACTIVE_SHELL) Shell shell,
						EPartService partService) {
		if (part == null)
			return;
		
		Object obj = part.getObject();
		if (!(obj instanceof Editor))
			return;
		
		if (findDialog == null) {
			findDialog = new FindDialog(shell, partService);
		}
		findDialog.open();
	}
	
	
	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_PART) MPart part) {
		if (part == null)
			return false;
		
		Object obj = part.getObject();
		if (!(obj instanceof Editor))
			return false;
		
		return true;
	}
		
}