 
package edu.rice.cs.hpcremote.ui;

import java.io.IOException;
import java.net.InetAddress;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.hpctoolkit.hpcclient.v1_0.HpcClientJavaNetHttp;

public class OpenRemoteDatabase {
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		
		GenericInteractiveDialog dialog = new GenericInteractiveDialog(
				shell, 
				"Connecting to remote host", 
				"Please enter the host id and the port number of the remote server", 
				new String[] {"Host:", "Port:"}, 
				new int[] {SWT.NONE, SWT.NONE});
		
		if (dialog.open() == Window.CANCEL)
			return;
		
		var inputs = dialog.getInputs();
		
		try {
			var addr = InetAddress.getByName(inputs[0]);
			int port = Integer.parseInt(inputs[1]);

			HpcClientJavaNetHttp client = new HpcClientJavaNetHttp(addr, port);
			var maxSamples = client.getMaximumTraceSampleTimestamp();
			MessageDialog.openInformation(shell, "Info", "max samples: " + maxSamples);

		} catch (IOException | NumberFormatException | InterruptedException e) {
			MessageDialog.openError(shell, 
					"Error connecting", 
					"Error message: " + e.getLocalizedMessage());
		}
		
	}
		
}