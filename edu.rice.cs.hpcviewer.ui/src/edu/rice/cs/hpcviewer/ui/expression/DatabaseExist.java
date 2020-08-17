 
package edu.rice.cs.hpcviewer.ui.expression;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Evaluate;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.base.IProfilePart;

public class DatabaseExist 
{
	@Inject DatabaseCollection database;
	
	@Evaluate
	public boolean evaluate(EPartService partService) {
		
		if (database.getNumDatabase()>0) {
			return (partService.getActivePart().getObject() instanceof IProfilePart);
		}
		return false;
	}
}
