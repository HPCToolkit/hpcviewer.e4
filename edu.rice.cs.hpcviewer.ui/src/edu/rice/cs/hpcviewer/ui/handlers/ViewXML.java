 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpcbase.ui.IBasePart;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.CanExecute;

public class ViewXML 
{
	@Inject EModelService 	   modelService;

	
	@Execute
	public void execute(@Optional @Active MPart part, MWindow window) {
		if (part != null) {
			Object obj = part.getObject();
			
			BaseExperiment experiment;
			
			// how to find "the" database:
			// - get the database of the current active view/editor
			// - or get the last opened database
			
			if (!(obj instanceof IBasePart)) {
				return;
			}
			experiment = ((IBasePart)obj).getExperiment();
			File file = experiment.getXMLExperimentFile();
			
			// sanity check: the file must exist
			
			if (file == null || !file.canRead())
				return;
			
			if (obj instanceof ProfilePart) {
				((ProfilePart)obj).addEditor(experiment);
				return;
			}
			// The current active element is trace view. 
			// find the corresponding profile part to display the XML file
			
			List<MPart> elements = modelService.findElements(part.getParent(), ProfilePart.ID, MPart.class); 
			for (MPart element: elements) {				

				ProfilePart profilePart = (ProfilePart) element.getObject();
				
				if (profilePart.getExperiment() == experiment) {
					profilePart.addEditor(experiment);
					
					// sanity check: make sure the profile part is visible
					element.setVisible(true);
					
					// activate the part
					part.getParent().setSelectedElement(element);
					
					return;
				}
			}
		}
	}
	
	
	@CanExecute
	public boolean canExecute(@Optional @Active MPart part) {
		if (part != null) {
			Object obj = part.getObject();
			if (obj instanceof IBasePart) {
				BaseExperiment experiment = ((IBasePart)obj).getExperiment();
				File file = experiment.getXMLExperimentFile();
				
				// we need to make sure the XML file really exist
				// for a merged database, we have a fake xml file. Hence, we shouldn't 
				// enable the menu if the current part is merged database.
				
				return (file != null && file.canRead());
			}
		}
		return false;
	}
		
}