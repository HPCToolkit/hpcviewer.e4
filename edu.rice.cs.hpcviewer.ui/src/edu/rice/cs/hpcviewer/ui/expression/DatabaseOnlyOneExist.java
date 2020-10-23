 
package edu.rice.cs.hpcviewer.ui.expression;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Evaluate;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public class DatabaseOnlyOneExist 
{
	@Inject DatabaseCollection database;
	
	@Evaluate
	public boolean evaluate(MWindow window) {
		int numDb = database.getNumDatabase(window);
		return numDb == 1;
	}
}
