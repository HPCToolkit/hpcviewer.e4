package edu.rice.cs.hpcbase;

import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcdata.experiment.IExperiment;

public interface IDatabase 
{
	enum DatabaseStatus {OK, INVALID, INEXISTENCE, UNKNOWN_ERROR, CANCEL}
	
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
}
