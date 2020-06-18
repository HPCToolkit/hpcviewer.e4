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

import edu.rice.cs.hpcviewer.ui.parts.IBasePart;


@Creatable
@Singleton
public class PartFactory 
{
	@Inject EModelService modelService; 
	@Inject EPartService partService;
	@Inject MApplication  app;

	public PartFactory() {}
	
	/****
	 * Main method to display the file.
	 * 
	 * @param stackId the parent of the part
	 * @param partDescriptorId the ID of the descriptor part
	 * @param elementId unique Id of the part
	 * @param input The object to display (either a scope or a database or others)
	 */
	public MPart display(String stackId,
						String partDescriptorId, 
						String elementId, 
						Object input) {
		
		if (input == null || elementId == null || partDescriptorId == null)
			return null;
		
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

				IBasePart objectPart = (IBasePart) shownPart.getObject();
				objectPart.setInput(shownPart, input);
				
				return shownPart;
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
		
			if (editorStack != null) {
				editorStack.getChildren().add(part);
			}
		}

		MPart shownPart = partService.showPart(part, PartState.VISIBLE);
		
		IBasePart objectPart = (IBasePart) shownPart.getObject();
		objectPart.setInput(shownPart, input);
		
		// show the label if needed
		if (shownPart.getLabel() == null)
			shownPart.setLabel(input.toString());
		
		return shownPart;
	}
}
