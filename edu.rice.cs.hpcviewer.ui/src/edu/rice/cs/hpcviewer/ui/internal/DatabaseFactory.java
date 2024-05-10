package edu.rice.cs.hpcviewer.ui.internal;

import java.nio.file.Paths;

import edu.rice.cs.hpcbase.IDatabase;
import edu.rice.cs.hpcbase.IDatabaseIdentification;
import edu.rice.cs.hpclocal.DatabaseLocal;
import edu.rice.cs.hpclocal.LocalDatabaseIdentification;
import edu.rice.cs.hpcremote.InvalidRemoteIdentficationException;
import edu.rice.cs.hpcremote.RemoteDatabaseIdentification;
import edu.rice.cs.hpcremote.data.DatabaseRemote;


public class DatabaseFactory 
{
	private DatabaseFactory() {
		// hide the constructor
	}
	
	public static IDatabase newInstance(IDatabaseIdentification databaseId) {
		if (isRemote(databaseId)) 
			return new DatabaseRemote();
		return new DatabaseLocal();
	}
	
	
	public static IDatabaseIdentification createDatabaseIdentification(String databaseId) {
		if (databaseId == null)
			return null;

		try {
			return new RemoteDatabaseIdentification(databaseId);
		} catch (InvalidRemoteIdentficationException e) {
			// it is NOT a remote id
		}
		
		var path = Paths.get(databaseId).toAbsolutePath();
		if (path.toFile().exists())
			return new LocalDatabaseIdentification(databaseId);
		
		return null;
	}
	
	/****
	 * Check if the id is for remote or not
	 * 
	 * @param databaseId
	 * 			The database id
	 * 
	 * @return {@code boolean} true if it's a remote database 
	 * 
	 */
	private static boolean isRemote(IDatabaseIdentification databaseId) {
		return (databaseId instanceof RemoteDatabaseIdentification);
	}
}
