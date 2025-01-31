// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.trace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.hpctoolkit.hpcclient.v1_0.BrokerClient;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcbase.IExecutionContextToNumberTracesMap;
import edu.rice.cs.hpcbase.IFilteredData;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;

public class RemoteFilteredData implements IFilteredData 
{
	private final IdTupleType idTupleType;
	private final List<IdTuple> listOriginalIdTuples;
	private final BrokerClient hpcClient;
	
	private IExecutionContextToNumberTracesMap mapIdTupleToSamples;
	private List<IdTuple> listIdTuples;
	private List<Integer> indexes;

	public RemoteFilteredData(BrokerClient hpcClient, List<IdTuple> listOriginalIdTuples, IdTupleType idTupleType) {
		this.hpcClient = hpcClient;
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
			return listOriginalIdTuples.size()-1;
		
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
	public IExecutionContextToNumberTracesMap getMapFromExecutionContextToNumberOfTraces() {
		if (mapIdTupleToSamples != null)
			return mapIdTupleToSamples;
		
		try {
			var samplesPerProfile = hpcClient.getNumberOfSamplesPerTrace();
			if (samplesPerProfile == null)
				// if the server fails to send the map, we return an empty map
				// this may happen if the database is corrupted or something wrong with the server
				return IExecutionContextToNumberTracesMap.EMPTY;
			
			var mapToSamples = new ObjectIntHashMap<>(getNumberOfRanks());
			
			for(var idTuple: listOriginalIdTuples) {
				if (idTuple != null)
					mapToSamples.put(idTuple, samplesPerProfile.getOrElse(idTuple.getProfileIndex()-1, 0));
			}
			mapIdTupleToSamples = mapToSamples::get;
			return mapIdTupleToSamples;
			
		} catch (InterruptedException e) {
			LoggerFactory.getLogger(getClass()).error("Getting trace map is interrupted", e);
		    Thread.currentThread().interrupt();
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).error("Error from the remote server", e);
		}
		return IExecutionContextToNumberTracesMap.EMPTY;
	}
}
