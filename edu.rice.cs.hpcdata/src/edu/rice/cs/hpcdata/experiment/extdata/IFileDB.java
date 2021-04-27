package edu.rice.cs.hpcdata.experiment.extdata;

import java.io.IOException;
import java.util.List;

import edu.rice.cs.hpcdata.db.IdTuple;


/*************************************************
 * 
 * Generic interface to read an external data 
 *
 *************************************************/
public interface IFileDB 
{
	public enum IdTupleOption {
		COMPLETE,
		BRIEF
	};
	
	/***
	 * Open a database containing the information of processes or threads.
	 * Users need to call this method first, before calling other methods.
	 * 
	 * @param filename String
	 * @param headerSize int
	 * @param recordSize int
	 * @throws IOException
	 */
	public void		open(String filename, int headerSize, int recordSize) throws IOException;

	/**
	 * Get the number of levels of parallelism.
	 * <ul>
	 * <li> If it's a sequential program, it returns 1 
 	 * <li> If it's pure MPI: it return 1
	 * <li> If it's hybrid MPI+OpenMP, it returns 2
	 * <li> etc.
	 * </ul>
	 * @return int
	 */
	public int 		getParallelismLevel();
	
	/***
	 * Get the number of processes or threads.
	 * 
	 * @return int
	 */
	public int 		getNumberOfRanks();
	
	/***
	 * retrieve the array of the label of rank IDs
	 * 
	 * @return String[]
	 */
	public String[]	getRankLabels();
	
	/****
	 * Retrieve the array of the offset for each rank in the file.
	 * @return long []
	 */
	public long[]	getOffsets();
	
	public List<IdTuple> getIdTuple(IdTupleOption option);
	public List<Short>   getIdTupleTypes();
	
	public long 	getLong  (long position) throws IOException;
	public int  	getInt   (long position) throws IOException;
	public double 	getDouble(long position) throws IOException;
	
	public long 	getMinLoc(int rank);
	public long 	getMaxLoc(int rank);
	
	public void		dispose();
}
