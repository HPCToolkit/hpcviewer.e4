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
			MessageDialog.openError(
					shell, 
					"Unable to open the database", 
					"Error message from the remote host:\n"
					+ message);
			return null;
		}
		
		return database;
	}
}
