package edu.rice.cs.hpcdata.experiment.extdata;

import java.util.List;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.experiment.extdata.IFileDB.IdTupleOption;

public interface IFilteredData extends IBaseData
{
	public boolean isGoodFilter();	
	public void    setIncludeIndex(List<Integer> listOfIncludedIndex);
	public List<IdTuple> getDenseListIdTuple(IdTupleOption option);
}
