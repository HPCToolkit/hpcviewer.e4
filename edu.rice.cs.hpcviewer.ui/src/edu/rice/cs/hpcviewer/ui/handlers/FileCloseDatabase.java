package edu.rice.cs.hpcviewer.ui.handlers;

import java.io.File;

import javax.inject.Inject;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseCollection;

public class FileCloseDatabase 
{
	@Inject DatabaseCollection database;
	
	@Execute
	/**
	public void execute() {
		System.out.println("huhhh");
	}
	*/
	public void execute(MDirectMenuItem menu) {

		if (database == null || database.isEmpty())
			return;
			
		if (menu == null)
			return;

		String element  = menu.getElementId();
		String filePath = element + File.separator + Constants.DATABASE_FILENAME;
		
		BaseExperiment exp = database.getExperiment(filePath);
		if (exp == null) {
			database.statusReport(IStatus.ERROR, filePath + ": Does not exist", null);
			return;
		}
		database.removeDatabase(exp);
	}
}
