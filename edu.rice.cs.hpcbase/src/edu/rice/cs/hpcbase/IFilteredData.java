// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcbase;

import java.util.List;

import org.hpctoolkit.db.local.db.IdTuple;
import org.hpctoolkit.db.local.db.IFileDB.IdTupleOption;


/**********************************
 * 
 * Interface for filtered execution contexts (a.k.a profiles)
 *
 **********************************/
public interface IFilteredData extends IBaseData
{
	/****
	 * Feed the list of the index of included id tuples.
	 * This method will change the output of {@link getListIdTuples}
	 * since it will return the filtered id tuples.
	 *  
	 * @param listOfIncludedIndex
	 */
	void setIncludeIndex(List<Integer> listOfIncludedIndex);

	
	/****
	 * Retrieve the list of original id tuples, not the filtered ones.
	 * 
	 * @param option
	 * 			An {@code IdTupleOption} object to refine what types of id tuple to return.
	 * 
	 * @return {@code List} of id tuples
	 * 
	 * @see IdTupleOption
	 * @see IdTuple
	 */
	List<IdTuple> getDenseListIdTuple(IdTupleOption option);
}
