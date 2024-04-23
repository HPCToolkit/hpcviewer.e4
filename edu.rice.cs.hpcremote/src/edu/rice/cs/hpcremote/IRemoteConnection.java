package edu.rice.cs.hpcremote;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcbase.IDatabase;
import edu.rice.cs.hpcremote.data.DatabaseRemote;

public interface IRemoteConnection 
{
	static IDatabase connect(Shell shell) {
		DatabaseRemote database = new DatabaseRemote();
		if (database.open(shell) != IDatabase.DatabaseStatus.OK) {
			if (database.getStatus() == IDatabase.DatabaseStatus.CANCEL)
				return null;
			
			var message = database.getErrorMessage();
			if (message == null || message.isEmpty()) {
				message = "Fail to connect or launch hpcserver.\nPlease make sure hpcserver is installed correctly.";
			} else {
				message = "Error message from the remote host:\n" + message;
			}
			MessageDialog.openError(
					shell, 
					"Unable to open the database", 
					message);
			return null;
		}
		
		return database;
	}
}
