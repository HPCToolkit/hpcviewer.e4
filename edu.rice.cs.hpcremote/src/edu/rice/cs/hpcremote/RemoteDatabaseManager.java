package edu.rice.cs.hpcremote;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcbase.IDatabase;
import edu.rice.cs.hpcremote.data.DatabaseRemote;

public class RemoteDatabaseManager 
{
	private RemoteDatabaseManager() {
		// hide the constructor
	}
	
	
	/***
	 * Try to open a database by asking the connection information,
	 * the user pass-phrase or password and browsing on remote host.
	 * 
	 * @param shell
	 * 			The current shell widget
	 * @return {@link IDatabase}
	 * 			The remote database object if the opening successful, {@code null} otherwise. 
	 */
	public static IDatabase connect(Shell shell) {
		DatabaseRemote database = new DatabaseRemote();
		if (database.open(shell) != IDatabase.DatabaseStatus.OK) {
			if (database.getStatus() == IDatabase.DatabaseStatus.CANCEL)
				return null;
			
			if (database.getStatus() == IDatabase.DatabaseStatus.NOT_RESPONSIVE) {
				// it looks like the connection is slow and can be very not-responsive
				if (MessageDialog.openConfirm(
						shell, 
						"Remote host " + database.getId(), 
						database.getErrorMessage() + "\n" + "Continue?"))
					return database;
				else
					return null;
			}
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
