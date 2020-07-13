package edu.rice.cs.hpctraceviewer.data;

import java.util.EnumMap;

/************************************************
 * 
 * Info needed for remote connection
 * This class acts as a hub between RemoteDBOpener and OpenDatabaseDialog
 *  
 * We need to reorganize the classes to make it more modular
 *
 ************************************************/
public class DatabaseAccessInfo 
{
  public static enum DatabaseField {DatabasePath, ServerName, ServerPort, 
	  // info needed for SSH tunneling
	  SSH_TunnelUsername, SSH_TunnelHostname, SSH_TunnelPassword};
	  
  
  private EnumMap<DatabaseField, String> fieldValues;
  
  // initialization for local database
  public DatabaseAccessInfo(String databasePath)
  {
	  fieldValues = new EnumMap<DatabaseField, String>(DatabaseField.class);
	  fieldValues.put(DatabaseField.DatabasePath, databasePath);
  }
  
  public DatabaseAccessInfo(EnumMap<DatabaseField, String> fields)
  {
	  this.fieldValues = fields;
  }
  
  static public EnumMap<DatabaseField, String> createEnumMap()
  {
	  return new EnumMap<DatabaseField, String>(DatabaseField.class);
	  
  }
  
  public String getField(DatabaseField field)
  {
	  return fieldValues.get(field);
  }
  
  public String getDatabasePath()
  {
	  return getField(DatabaseField.DatabasePath);
  }
  
  /************
   * check if ssh tunnel is enabled.
   * Currently we define an SSH tunnel is enabled if the hostname is defined.
   * This is not a perfect definition, but we can avoid redundant variables.
   * 
   * @return true if ssh tunnel should be enabled
   */
  public boolean isTunnelEnabled()
  {
	  return (fieldValues.containsKey(DatabaseField.SSH_TunnelHostname));
  }
  
  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() 
  {
	  if (isLocal()) {
		  return fieldValues.get(DatabaseField.DatabasePath);
	  } else {
		  String result = "Hostname: " + fieldValues.get(DatabaseField.ServerName) + 
				  ":" + fieldValues.get(DatabaseField.ServerPort) + 
				  " @ " + fieldValues.get(DatabaseField.DatabasePath);
		  if (isTunnelEnabled()) {
			  result += "\nSSH: " + fieldValues.get(DatabaseField.SSH_TunnelUsername) +
					  " @ " + fieldValues.get(DatabaseField.SSH_TunnelHostname);
		  }
		  return result;
	  }
  }
  
  public boolean isLocal() 
  {
	  if (!fieldValues.containsKey(DatabaseField.ServerName)) {
		  if (fieldValues.containsKey(DatabaseField.DatabasePath))
			  return true;
		  
		  // both local database and remote information cannot be null
		  // if this is the case, it should be error in code design ! 
		  
		  throw new RuntimeException("Path to the local database is null");
	  }
	  return false;
  }
}
