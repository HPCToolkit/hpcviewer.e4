 
package edu.rice.cs.hpcremote;

import java.io.IOException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import edu.rice.cs.hpcdata.experiment.Experiment;

import edu.rice.cs.hpcremote.data.RemoteDatabaseParser;
import edu.rice.cs.hpcremote.ui.ConnectionDialog;

public class RemoteDatabaseManager 
{
	
	/****
	 * 
	 * @param shell
	 * @return
	 */
	public Experiment openRemoteDatabase(Shell shell) {
		do {
			var dialog = new ConnectionDialog(shell);
			
			if (dialog.open() == Window.CANCEL)
				return null;
			
			var remoteInfo = dialog.getRemoteInfo();
			var client = remoteInfo.getClient();
			if (client == null)
				return null;
			
			RemoteDatabaseParser parser = new RemoteDatabaseParser();
			
			try {
				parser.parse(client);
	
				return parser.getExperiment();
	
			} catch (IOException | NumberFormatException | InterruptedException e) {
				MessageDialog.openError(shell, 
						"Error connecting", 
						"Error message: " + e.getLocalizedMessage());
			} catch (Exception e) {
				MessageDialog.openError(shell, "Unknown error", e.getMessage());
			}
		} while (true);
	}
}