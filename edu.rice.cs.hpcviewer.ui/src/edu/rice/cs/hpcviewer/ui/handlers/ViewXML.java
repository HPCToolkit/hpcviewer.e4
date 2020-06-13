 
package edu.rice.cs.hpcviewer.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.parts.IBasePart;
import edu.rice.cs.hpcviewer.ui.parts.editor.Editor;
import edu.rice.cs.hpcviewer.ui.parts.editor.PartFactory;

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
	public void execute(@Active MPart part) {
		if (part != null) {
			Object obj = part.getObject();
			
			if (obj instanceof IBasePart) {
				BaseExperiment experiment = ((IBasePart)obj).getExperiment();
				String elementId = Editor.getTitle(experiment);
				partFactory.display(Editor.STACK_ID, Editor.ID_DESC, elementId, experiment);
			}
		}
	}
	
	
	@CanExecute
	public boolean canExecute() {
		
		return !database.isEmpty();
	}
		
}