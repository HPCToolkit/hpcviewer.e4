package edu.rice.cs.hpcviewer.ui.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public class FileAddDatabase 
{
	
	@Inject EPartService partService;
	@Inject IEventBroker broker;
	@Inject MApplication application;
	@Inject EModelService modelService;
	
	@Inject DatabaseCollection databaseCollection;
	
	@Execute
	public void execute( @Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		
		databaseCollection.addDatabase(shell, application, partService, modelService, null);
	}

}
