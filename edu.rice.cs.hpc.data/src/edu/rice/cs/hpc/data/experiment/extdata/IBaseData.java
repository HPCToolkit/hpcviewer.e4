package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.IOException;
import java.util.List;

import edu.rice.cs.hpc.data.db.IdTuple;
import edu.rice.cs.hpc.data.experiment.extdata.IFileDB.IdTupleOption;

/*************************************************************************
 * 
 * Interface for managing the access to "external" data
 * Note: External here means outside xml file
 *
 *************************************************************************/
public interface IBaseData 
{	
	/***
	 * Retrieve the list of id tuples of this database.
	 * @return {@code List<IdTuple>}
	 */
	public List<IdTuple> getListOfIdTuples(IdTupleOption option);
	
	/****
	 * retrieve the number of ranks 
	 * @return
	 */
	public int getNumberOfRanks();
	
	/**
	 * Get the index of the first included rank. Provided to give a
	 * window through the filtering abstraction
	 */
	public int getFirstIncluded();
	
	/***
	 * Get the last inclusive rank
	 * @return int rank
	 */
	public int getLastIncluded();

	/** Is every rank included between the first and the last as provided above?*/
	public boolean isDenseBetweenFirstAndLast();
	
	/** Return true if the application is a hybrid app (such as MPI+OpenMP). 
	 *  False otherwise
	 *  @return boolean **/
	public boolean isHybridRank();
	
	
	/***
	 * Generalized version of {@code isHybridRank}.
	 * This method returns the number of parallelism level of the profiled applications.
	 * For pure MPI, it returns 1, for hybrid MPI+OpenMP it returns 2, ...
	 * @return int
	 */
	public int getNumLevels();
	
	/****
	 * Disposing native resources, to be called by the caller
	 */
	public void dispose();

	/*****
	 * retrieve a 64-bytes data for a given location
	 * @param position
	 * @return
	 * @throws IOException
	 */
	long getLong(long position) throws IOException;

	/********
	 * Retrieve a 32-bytes data for a given location
	 * @param position
	 * @return
	 * @throws IOException
	 */
	int getInt(long position) throws IOException;
	
	/********
	 * Get the size of the record
	 * @return
	 */
	int getRecordSize();

	/*******
	 * get the start offset (location) of a given rank
	 * @param rank
	 * @return
	 */
	public long getMinLoc(int rank);

	/*******
	 * get the end offset (location) of a given rank
	 * @param rank
	 * @return
	 */
	public long getMaxLoc(int rank);
}
