package edu.rice.cs.hpcbase;

import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcdata.experiment.IExperiment;

public interface IDatabase 
{
	enum DatabaseStatus {NOT_INITIALIZED, OK, INVALID, INEXISTENCE, UNKNOWN_ERROR, CANCEL}
	
	/****
	 * Get the unique ID of this database. 
	 * It can be the full path of the file or the remote access + full path.
	 * 
	 * @return {@code String} 
	 * 			The unique ID
	 */
	String getId();
	
	
	/****
	 * Check if the database is valid and can be used by the viewer
	 * 
	 * @param shell
	 * 			The parent shell to display a message to the user
	 * 
	 * @return {@code boolean} 
	 * 			true if the database is valid
	 */
	DatabaseStatus open(Shell shell);
	
	
	/****
	 * Get the latest status of this database
	 * 
	 * @return {@code DatabaseStatus}
	 */
	DatabaseStatus getStatus();
	
	/****
	 * Inform the class that the database is not needed anymore and we can
	 * start clean up the allocated resources if any.
	 */
	void close();
	
	/*****
	 * Retrieve the {@code IExperiment} object when the initialization succeeds.
	 * Otherwise it returns null.
	 * 
	 * @return {@code IExperiment}
	 * 			null if the initialization fails.
	 */
	IExperiment getExperimentObject();
	
	
	/****
	 * Check if the database has traces or not
	 * 
	 * @return {@code boolean}
	 * 			{@code true} if it includes traces, {@code false} otherwise. 
	 */
	boolean hasTraceData();
}
