package edu.rice.cs.hpcviewer.ui;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import edu.rice.cs.hpcviewer.ui.handlers.NewWindow;


@Creatable
@Singleton
public class Perspective 
{
	private MUIElement clonedPerspective;

	
	@Inject
	@Optional
	public void subscribeApplicationCompleted(
			@UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE)
			final MApplication application,
			final EModelService modelService) {
		
		MWindow window = (MWindow) modelService.find(NewWindow.ID_WINDOW, application);

		 MPerspective activePerspective = modelService.getActivePerspective(window);
		 if (activePerspective == null)
			 return;
		 
        // You must clone the perspective as snippet, otherwise the running
        // application would break, because the saving process of the resource
        // removes the element from the running application model
		 clonedPerspective = modelService.cloneElement(activePerspective, window);
	}
	
	public void resetPerspective(
			final EPartService  partService,
			final EModelService modelService,
			final MWindow       window) {
		
		// get the parent perspective stack, so that the loaded
        // perspective can be added to it.
        MPerspective activePerspective = modelService.getActivePerspective(window);
        MElementContainer<MUIElement> perspectiveParent = activePerspective.getParent();

        // remove the current perspective, which should be replaced by
        // the loaded one
        List<MPerspective> alreadyPresentPerspective = modelService.findElements(window,
        		clonedPerspective.getElementId(), MPerspective.class, null);
        
        for (MPerspective perspective : alreadyPresentPerspective) {
            modelService.removePerspectiveModel(perspective, window);
        }

        // add the loaded perspective and switch to it
        perspectiveParent.getChildren().add(clonedPerspective);

        partService.switchPerspective((MPerspective) clonedPerspective);
	}
}
