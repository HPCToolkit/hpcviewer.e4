package edu.rice.cs.hpcviewer.ui.parts.editor;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import edu.rice.cs.hpcviewer.ui.parts.ProfilePart;


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
						String elementId, 
						Object input) {
		
		if (input == null || elementId == null )
			return null;
		
		// ----------------------------------------------------------
		// check if the editor (or part) is already created.
		// if this is the case, make it visible
		// otherwise, we create a new one
		// ----------------------------------------------------------

		MPart part = partService.getActivePart();
		if (part == null)
			return null;
		
		Object obj = part.getObject();
		if (!(obj instanceof ProfilePart))
			return part;

		ProfilePart profilePart = (ProfilePart) part.getObject();
		profilePart.addEditor(input);
		
		return part;
	}
}
