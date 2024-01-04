package edu.rice.cs.hpcremote;

import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcbase.IDatabase;
import edu.rice.cs.hpcremote.data.DatabaseRemote;

public interface IRemoteConnection 
{
	static IDatabase connect(Shell shell) {
		DatabaseRemote database = new DatabaseRemote();
		if (database.open(shell) != IDatabase.DatabaseStatus.OK)
			return null;
		
		return database;
	}
}
