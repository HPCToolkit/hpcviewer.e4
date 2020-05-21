package edu.rice.cs.hpcviewer.ui.handlers;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessRemovals;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpcviewer.ui.experiment.DatabaseManager;
import edu.rice.cs.hpcviewer.ui.experiment.ExperimentManager;

public class LifeCycle 
{
	@Inject EPartService partService;
	@Inject IEventBroker broker;
	@Inject EModelService modelService;

	@Inject DatabaseManager expApplication;


	@PostContextCreate
	public void startup(IEclipseContext context, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		
		String args[] = Platform.getApplicationArgs();
		
		Shell myShell = shell;
		if (myShell == null) {
			myShell = new Shell(SWT.TOOL | SWT.NO_TRIM);
		}
		BaseExperiment experiment    = null;
		ExperimentManager expManager = new ExperimentManager();
		
		String path = null;
		
		for (String arg: args) {
			if (arg.charAt(0) != '-')
				path = arg;
		}
		if (path == null || path.length() < 1) {
			experiment    = expManager.openFileExperiment(myShell);
		} else {
			experiment = openDatabase(myShell, expManager, args[0]);
		}
		if (experiment == null)
			return;
		
		expApplication.addDatabase(experiment, null, context, broker, modelService);
	}
	

	@PreSave
	void preSave(IEclipseContext workbenchContext) {
	}

	@ProcessAdditions
	void processAdditions(IEclipseContext workbenchContext) {
	}

	@ProcessRemovals
	void processRemovals(IEclipseContext workbenchContext) {}

	private BaseExperiment openDatabase(Shell shell, ExperimentManager expManager, String sPath) {
    	IFileStore fileStore;

		try {
			fileStore = EFS.getLocalFileSystem().getStore(new URI(sPath));
		} catch (URISyntaxException e) {
			// somehow, URI may throw an exception for certain schemes. 
			// in this case, let's do it traditional way
			fileStore = EFS.getLocalFileSystem().getStore(new Path(sPath));
			e.printStackTrace();
		}
    	IFileInfo objFileInfo = fileStore.fetchInfo();

    	if (!objFileInfo.exists())
    		return null;

    	BaseExperiment experiment = null;
    	
    	if (objFileInfo.isDirectory()) {
    		experiment = expManager.openDatabaseFromDirectory(shell, sPath);
    	} else {
			EFS.getLocalFileSystem().fromLocalFile(new File(sPath));
			experiment = expManager.loadExperiment(shell, sPath);
    	}
    	return experiment;
	}
}
