 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public class ResetLayout 
{
	static final private String PERSPECTIVE_ID = "edu.rice.cs.hpcviewer.ui.perspective.main";
	
	@Execute
	public void execute(EModelService service, MWindow window, MApplication application) {
		
		MUIElement element = service.find(PERSPECTIVE_ID, application);
		
		if (element == null)
			return;
		
		// cannot use resetPerspective due to bug:
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=404231
		// service.resetPerspectiveModel((MPerspective) element, window);

		EPartService partService = window.getContext().get(EPartService.class);
		partService.switchPerspective((MPerspective) element);
	}
		
}