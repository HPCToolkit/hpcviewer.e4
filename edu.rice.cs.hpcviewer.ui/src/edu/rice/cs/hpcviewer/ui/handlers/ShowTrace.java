 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.e4.core.di.annotations.CanExecute;

import javax.inject.Named;

import edu.rice.cs.hpctraceviewer.ui.main.HPCTraceView;

public class ShowTrace 
{
	@Execute
	public void execute(EModelService ms, 
			MApplication  application, 
			MWindow       window,
			EPartService  partService,
			EModelService modelService,
			@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {

		MWindow winTrace = (MWindow) modelService.find(HPCTraceView.ID_WINDOW, application);
		
		MPerspective perspective = (MPerspective) modelService.find(HPCTraceView.ID_PERSPECTIVE, application);
		
		if (perspective == null || winTrace == null)
			return;
		
		int height = window.getHeight();
		int width  = window.getWidth();
		int x = window.getX() + 100;
		int y = window.getY() + 100;
		
		winTrace.setX(x);
		winTrace.setY(y);
		winTrace.setHeight(height);
		winTrace.setWidth(width);

		winTrace.setVisible(true);
		winTrace.setOnTop(true);
		
	}
	
	
	@CanExecute
	public boolean canExecute() {
		
		return true;
	}
		
}