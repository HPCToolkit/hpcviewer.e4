package edu.rice.cs.hpcbase;

import java.util.List;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;

/*************************************************************************
 *
 * Interface for managing the access to "external" data
 * where the term "External" here means outside xml file.
 * <p>
 * This class is the abstraction to access any kind of data:
 * dense, filter and even remote data (see hpcremote plugin).
 * </p>
 *
 *************************************************************************/
public interface IBaseData
{
	/***
	 * Retrieve the list of id tuples of this database.
	 * @return {@code List<IdTuple>}
	 */
	List<IdTuple> getListOfIdTuples(IdTupleOption option);


	/****
	 * Retrieve the map of execution context id-tuple to its number of trace samples
	 * 
	 * @return {@code Map<IdTuple, Integer>}
	 * 
	 * @apiNote It is advised to invoke this function one and 
	 * 			store the map since the map can be huge.
	 */
	IExecutionContextToNumberTracesMap getMapFromExecutionContextToNumberOfTraces();
	
	/**
	 * Retrieve the list of types of id tuples
	 * @return
	 */
	IdTupleType  getIdTupleTypes();

	/****
	 * retrieve the number of ranks
	 * @return
	 */
	int getNumberOfRanks();

	/**
	 * Get the index of the first included rank. Provided to give a
	 * window through the filtering abstraction
	 */
	int getFirstIncluded();

	/***
	 * Get the last inclusive rank
	 * @return int rank
	 */
	int getLastIncluded();

	/** Is every rank included between the first and the last as provided above?*/
	boolean isDenseBetweenFirstAndLast();


	/***
	 * Return {@code true} if the database has GPU measurements
	 * {@code false} otherwise.
	 * @return {@code boolean}
	 */
	boolean hasGPU();

	/****
	 * Disposing native resources, to be called by the caller
	 */
	void dispose();

	/***
	 * Check of the specified "execution context" is a GPU stream.
	 * @param rank
	 * 			The "execution context"
	 * @return
	 */
	boolean isGPU(int rank);
}
