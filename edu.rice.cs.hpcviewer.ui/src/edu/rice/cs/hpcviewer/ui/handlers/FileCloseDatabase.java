package edu.rice.cs.hpcviewer.ui.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.parts.IBasePart;

public class FileCloseDatabase 
{
	@Inject DatabaseCollection database;
	@Inject EPartService       partService;
	
	@Execute
	public void execute() {

		if (database == null || database.isEmpty())
			return;
		
		MPart part = partService.getActivePart();
		if (part == null) {
			// no part is active: should we remove all?
			database.removeAll();
			
			return;
		}
		Object obj = part.getObject();
		
		if (obj != null && obj instanceof IBasePart) {
			database.removeDatabase(((IBasePart)obj).getExperiment());
		}
	}
}
