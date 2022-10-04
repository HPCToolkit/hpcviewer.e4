package edu.rice.cs.hpcdata.experiment;

import java.io.File;

/***************************************
 * 
 * Basic interface to represent a database either local or remote.
 *
 ***************************************/
public interface IDatabaseRepresentation 
{
	public File getFile();
	
	public void setFile(File file);
	
	public void open(IExperiment experiment) throws	Exception;
	
	public IDatabaseRepresentation duplicate();
	
	/****
	 * Return the version of the database if the database is correct and valid
	 * 			   return negative number otherwise
	 * 
	 * @return int 
	 * 
	 * @see Constants.EXPERIMENT_SPARSE_VERSION
	 * @see Constants.EXPERIMENT_DENSED_VERSION
	 */
	public int getTraceDataVersion();
}
