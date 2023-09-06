package edu.rice.cs.hpcbase;

public interface IDatabaseLocal extends IDatabase 
{
	String getDirectory();
	
	void setDirectory(String directory);
}
