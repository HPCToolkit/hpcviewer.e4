package edu.rice.cs.hpcviewer.ui.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;

public class FileCloseDatabase 
{
	@Inject DatabaseCollection database;
	
	@Execute
	public void execute() {

		if (database == null || database.isEmpty())
			return;
		
	}
}
