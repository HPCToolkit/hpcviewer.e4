package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.IOException;


/*************************************************
 * 
 * Generic interface to read an external data 
 *
 *************************************************/
public interface IFileDB 
{
	public void		open(String filename, int headerSize, int recordSize) throws IOException;
	
	public int 		getNumberOfRanks();
	public String[]	getRankLabels();
	public long[]	getOffsets();
	
	public long 	getLong  (long position) throws IOException;
	public int  	getInt   (long position) throws IOException;
	public double 	getDouble(long position) throws IOException;
	
	public int 		getParallelismLevel();
	
	public long 	getMinLoc(int rank);
	public long 	getMaxLoc(int rank);
	
	public void		dispose();
}
