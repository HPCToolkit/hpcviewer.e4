package edu.rice.cs.hpcdata.experiment.extdata;

import java.io.IOException;
import java.util.List;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.experiment.extdata.IFileDB.IdTupleOption;

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

	/***
	 * Set new list of id tuples to replace the existing one.
	 * This method is useful to filter a list of id tuples and
	 * replace the existing one.
	 * <p>
	 * TODO: Should be moved to IFilteredData interface
	 * @param listIdTuples {@code List<IdTuple>}
	 */
	void setListOfIdTuples(List<IdTuple> listIdTuples);


	/**
	 * Retrieve the list of types of id tuples
	 * @return
	 */
	public IdTupleType  getIdTupleTypes();

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
	 * Return {@code true} if the database has GPU measurements
	 * {@code false} otherwise.
	 * @return {@code boolean}
	 */
	public boolean hasGPU();
	
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
