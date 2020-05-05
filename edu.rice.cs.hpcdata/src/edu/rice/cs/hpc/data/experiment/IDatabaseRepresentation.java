package edu.rice.cs.hpc.data.experiment;

import java.io.File;


public interface IDatabaseRepresentation 
{
	public File getFile();
	public void setFile(File file);
	
	public void open(BaseExperiment experiment) throws	Exception;
	public IDatabaseRepresentation duplicate();
}
