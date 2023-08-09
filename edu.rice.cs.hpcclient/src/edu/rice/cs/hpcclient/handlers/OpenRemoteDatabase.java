 
package edu.rice.cs.hpcclient.handlers;

import java.io.IOException;
import java.net.InetAddress;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.hpctoolkit.hpcclient.v1_0.HpcClientJavaNetHttp;

public class OpenRemoteDatabase {
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		
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