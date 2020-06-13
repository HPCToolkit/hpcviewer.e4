package edu.rice.cs.hpcviewer.ui.parts.editor;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;


@Creatable
@Singleton
public class PartFactory 
{
	@Inject EModelService modelService; 
	@Inject EPartService partService;
	@Inject MApplication  app;

	public PartFactory() {
		System.out.println("PartFactory ms: " + modelService + ", ps: " + partService +", app: " + app);
	}
	
	/****
	 * Main method to display the file.
	 * 
	 * @param modelService
	 * @param partService
	 * @param app
	 * @param obj The object to display (either a scope or a database)
	 */
	public void display(String stackId,
						String partDescriptorId, 
						String elementId, 
						Object obj) {
		
		if (obj == null || elementId == null || partDescriptorId == null)
			return;
		
		// ----------------------------------------------------------
		// check if the editor (or part) is already created.
		// if this is the case, make it visible
		// otherwise, we create a new one
		// ----------------------------------------------------------
		
		Collection<MPart> listParts = partService.getParts();
		for(MPart mp : listParts) {
			if (mp.getElementId().equals(elementId)) {
				
				if (mp.getObject() == null) {
					partService.showPart(mp, PartState.CREATE);
				}
				MPart shownPart = partService.showPart(mp, PartState.VISIBLE);

				IUpperPart editor = (IUpperPart) shownPart.getObject();
				editor.display(obj);
				
				return;
			}
		}

		// ----------------------------------------------------------
		// case where the part is not created:
		// - create a new part
		// - add it to the stack
		// - display the object
		// ----------------------------------------------------------

		final MPart part = partService.createPart(partDescriptorId);
		part.setElementId(elementId);

		if (stackId != null) {
			MPartStack editorStack = (MPartStack)modelService.find(stackId, app);
			editorStack.getChildren().add(part);
		}

		MPart shownPart = partService.showPart(part, PartState.VISIBLE);
		
		IUpperPart editor = (IUpperPart) shownPart.getObject();
		editor.display(obj);
		part.setLabel(editor.getTitle());
	}
}
