 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpcbase.ui.IBasePart;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.parts.editor.PartFactory;
import java.io.File;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.CanExecute;

public class ViewXML 
{
	@Inject DatabaseCollection database;
	@Inject EPartService       partService;
	@Inject MApplication 	   application;
	@Inject EModelService 	   modelService;

	@Inject PartFactory        partFactory;
	
	@Execute
	public void execute(@Optional @Active MPart part, MWindow window) {
		if (part != null) {
			Object obj = part.getObject();
			
			BaseExperiment experiment;
			
			// how to find "the" database:
			// - get the database of the current active view/editor
			// - or get the last opened database
			
			if (obj instanceof IBasePart) {
				experiment = ((IBasePart)obj).getExperiment();
			} else {
				experiment = database.getLast();
			}
			File file = experiment.getXMLExperimentFile();
			
			// sanity check: the file must exist
			
			if (file == null || !file.canRead())
				return;
			
			if (obj instanceof ProfilePart) {
				((ProfilePart)obj).addEditor(experiment);
				return;
			}
			// find the corresponding profile part to display the XML file
			
			MUIElement element = modelService.find(ProfilePart.ID, window);
			if (element == null)
				return;
			if (!(element instanceof MPart))
				return;
			
			ProfilePart profilePart = (ProfilePart) ((MPart)element).getObject();
			profilePart.addEditor(experiment);
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
		return !database.isEmpty();
	}
		
}