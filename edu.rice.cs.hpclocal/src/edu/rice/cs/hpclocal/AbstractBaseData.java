package edu.rice.cs.hpclocal;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.rice.cs.hpcbase.BaseConstants;
import edu.rice.cs.hpcdata.db.IFileDB;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;


/*********************************************************
 * 
 * Abstract class to manage trace data. 
 * This class is the parent for all regular data and filtered data
 *
 *********************************************************/
public abstract class AbstractBaseData implements ILocalBaseData 
{
	protected final IFileDB baseDataFile;
	private Map<IdTuple, Integer> mapTraceToRecord;

	protected AbstractBaseData(IFileDB baseDataFile){
		this.baseDataFile = baseDataFile;
	}
	
	
	@Override
	public List<IdTuple> getListOfIdTuples(IdTupleOption option) {
		return baseDataFile.getIdTuple(option);
	}


	@Override
	public IdTupleType   getIdTupleTypes() {
		return baseDataFile.getIdTupleTypes();
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getLong(long)
	 */
	@Override
	public long getLong(long position) throws IOException {
		return baseDataFile.getLong(position);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getInt(long)
	 */
	@Override
	public int getInt(long position) throws IOException {
		return baseDataFile.getInt(position);
	}

	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#dispose()
	 */
	@Override
	public void dispose() {
		if (baseDataFile != null)
			baseDataFile.dispose();
		
		if (mapTraceToRecord != null)
			mapTraceToRecord.clear();
		
		mapTraceToRecord = null;
	}
	

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getRecordSize()
	 */
	@Override
	public int getRecordSize() {
		return BaseConstants.TRACE_RECORD_SIZE;
	}

	@Override
	public boolean hasGPU() {
		return baseDataFile.hasGPU();
	}
	
	@Override
	public boolean isGPU(int rank) {
		return baseDataFile.isGPU(rank);
	}



	@Override
	public Map<IdTuple, Integer> getMapFromExecutionContextToNumberOfTraces() {
		if (mapTraceToRecord != null)
			return mapTraceToRecord;
		
		mapTraceToRecord  = new HashMap<>();

		var listIdTuples = getListOfIdTuples(IdTupleOption.BRIEF);		
		
		for(var idt: listIdTuples) {
			var min = baseDataFile.getMinLoc(idt);
			var max = baseDataFile.getMaxLoc(idt);
			int records = (int) ((max-min) / getRecordSize());
			
			mapTraceToRecord.put(idt, records);
		}
		return mapTraceToRecord;
	}
}
