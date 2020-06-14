 
package edu.rice.cs.hpcviewer.ui.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import edu.rice.cs.hpcviewer.ui.Perspective;

public class ResetLayout 
{
	@Inject Perspective perspective;
	
	@Execute
	public void execute(EPartService partService, EModelService service, MWindow window) {
		
		// cannot use resetPerspective due to bug:
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=404231
		// service.resetPerspectiveModel((MPerspective) element, window);

		//perspective.resetPerspective(partService, service, window);
	}
		
}