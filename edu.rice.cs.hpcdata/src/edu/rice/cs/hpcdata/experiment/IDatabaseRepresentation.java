package edu.rice.cs.hpcdata.experiment;

import java.io.File;


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
	 */
	public int getTraceDataVersion();
}
