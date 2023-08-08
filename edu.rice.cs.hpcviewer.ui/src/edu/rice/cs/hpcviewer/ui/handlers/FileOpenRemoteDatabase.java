package edu.rice.cs.hpcviewer.ui.handlers;

import java.io.IOException;
import java.net.InetAddress;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.hpctoolkit.hpcclient.v1_0.HpcClientJavaNetHttp;

import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;

public class FileOpenRemoteDatabase 
{
	@Inject MApplication application;
	@Inject EModelService modelService;
	
	@Inject DatabaseCollection databaseCollection;

	@Execute
	public void execute(IWorkbench workbench, 
						IEclipseContext context, 
						EPartService partService,
						MWindow window,
						@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		
		HpcClientJavaNetHttp client = null;
        try {
			client = new HpcClientJavaNetHttp(InetAddress.getLocalHost(), 61599);
			var maxSamples = client.getMaximumTraceSampleTimestamp();
			MessageDialog.openInformation(shell, "Info", "max samples: " + maxSamples);
		} catch (IOException | InterruptedException e) {
			MessageDialog.openError(shell, 
									"Error connecting" + client, 
									"Error message: " + e.getLocalizedMessage());
		}
	}

}
