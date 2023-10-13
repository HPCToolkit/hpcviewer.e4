package edu.rice.cs.hpcbase;

import java.util.List;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;

public interface IFilteredData extends IBaseData
{
	boolean isGoodFilter();
	void    setIncludeIndex(List<Integer> listOfIncludedIndex);

	List<IdTuple> getDenseListIdTuple(IdTupleOption option);
}
