package edu.rice.cs.hpcremote.trace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.rice.cs.hpcbase.IFilteredData;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;

public class RemoteFilteredData implements IFilteredData 
{
	final IdTupleType idTupleType;
	final List<IdTuple> listOriginalIdTuples;
	
	List<IdTuple> listIdTuples;
	List<Integer> indexes;

	public RemoteFilteredData(List<IdTuple> listOriginalIdTuples, IdTupleType idTupleType) {
		this.listOriginalIdTuples = listOriginalIdTuples;
		this.idTupleType = idTupleType;
	}
	

	@Override
	public List<IdTuple> getListOfIdTuples(IdTupleOption option) {
		if (indexes == null) {
			// this can happen when we remove the filter and go back to the densed one.
			return listOriginalIdTuples;
		}
		if (listIdTuples != null) {
			return listIdTuples;
		}
		listIdTuples = new ArrayList<>();
		
		for (int i=0; i<indexes.size(); i++) {
			Integer index = indexes.get(i);
			IdTuple idTuple = listOriginalIdTuples.get(index);
			listIdTuples.add(idTuple);
		}
		return listIdTuples;
	}


	@Override
	public IdTupleType getIdTupleTypes() {
		return idTupleType;
	}

	@Override
	public int getNumberOfRanks() {
		return getListOfIdTuples(IdTupleOption.BRIEF).size();
	}

	@Override
	public int getFirstIncluded() {
		if (indexes == null || indexes.isEmpty())
			return 0;
		
		return indexes.get(0);
	}

	@Override
	public int getLastIncluded() {
		if (indexes == null || indexes.isEmpty())
			return 0;
		
		return indexes.get(indexes.size()-1);
	}

	@Override
	public boolean isDenseBetweenFirstAndLast() {
		if (indexes == null || indexes.isEmpty())
			return true;
		
		int size = indexes.size();
		return indexes.get(size-1)-indexes.get(0) == size-1;
	}

	@Override
	public boolean hasGPU() {
		var list = getListOfIdTuples(IdTupleOption.BRIEF);
		var gpuIdTuple = list.stream().filter( idt -> idt.isGPU(idTupleType)).findAny();
		return gpuIdTuple.isPresent();
	}

	@Override
	public void dispose() {
		indexes = null;
		
		if (listIdTuples != null)
			listIdTuples.clear();
		listIdTuples = null;
	}

	@Override
	public boolean isGPU(int rank) {
		if (indexes == null)
			return false;
		
		var index = indexes.get(rank);
		var list  = getListOfIdTuples(IdTupleOption.BRIEF);
		var idtuple = list.get(index);
		
		return idtuple.isGPU(idTupleType);
	}

	@Override
	public void setIncludeIndex(List<Integer> listOfIncludedIndex) {
		indexes = listOfIncludedIndex;
		listIdTuples = null;			
	}

	@Override
	public List<IdTuple> getDenseListIdTuple(IdTupleOption option) {
		return listOriginalIdTuples;
	}


	@Override
	public Map<IdTuple, Integer> getMapFromExecutionContextToNumberOfTraces() {
		throw new IllegalAccessError("Not yet supported for remote database");
	}
}
