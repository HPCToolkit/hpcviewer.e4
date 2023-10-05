 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpcbase.IEditorInput;
import edu.rice.cs.hpcbase.ui.IBasePart;
import edu.rice.cs.hpcbase.ui.IProfilePart;
import edu.rice.cs.hpcbase.ui.IUpperPart;
import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
			
			var file = getExperimentFile(obj);

			// sanity check: the file must exist
			if (file == null || !file.canRead())
				return;
			
			if (obj instanceof IProfilePart) {
				displayXML((IProfilePart) obj, file);
				return;
			}
			// The current active element is trace view. 
			// find the corresponding profile part to display the XML file
			
			List<MPart> elements = modelService.findElements(part.getParent(), ProfilePart.ID, MPart.class); 
			for (MPart element: elements) {				

				ProfilePart profilePart = (ProfilePart) element.getObject();
				var database = profilePart.getInput();
				var experiment = database.getExperimentObject();
				
				if (profilePart.getExperiment() == experiment) {
					displayXML(profilePart, file);
					
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
		var expFile = getExperimentFile(part.getObject());
		return expFile != null && expFile.canRead();
	}

	
	private void displayXML(IProfilePart profilePart, File file) {
		var input = new IEditorInput() {
			
			@Override
			public String getShortName() {
				return file.getName();
			}
			
			@Override
			public String getLongName() {
				return file.getAbsolutePath();
			}
			
			@Override
			public String getId() {
				return file.getPath();
			}
			
			@Override
			public boolean needToTrackActivatedView() {
				return false;
			}
			
			@Override
			public IUpperPart createViewer(Composite parent) {
				return null;
			}
			
			@Override
			public int getLine() {
				return 1;
			}
			
			@Override
			public String getContent() {
				Path path = Path.of(file.getAbsolutePath());
				try {
					return Files.readString(path, StandardCharsets.ISO_8859_1);
				} catch (IOException e) {
					profilePart.showErrorMessage(e.getMessage());
				}
				return null;
			}
		};
		profilePart.addEditor(input);
	}
	
	
	private File getExperimentFile(Object obj) {
		if (obj instanceof IBasePart) {
			var database = ((IBasePart)obj).getInput();
			if (database == null || (database instanceof LocalDatabaseRepresentation))
				return null;
			
			var experiment = database.getExperimentObject();
			if (!(experiment instanceof BaseExperiment))
				return null;
			
			File file = ((BaseExperiment) experiment).getExperimentFile();
			
			// we need to make sure the XML file really exist
			// for a merged database, we have a fake xml file. Hence, we shouldn't 
			// enable the menu if the current part is merged database.
			
			return file;
		}
		return null;
	}
}