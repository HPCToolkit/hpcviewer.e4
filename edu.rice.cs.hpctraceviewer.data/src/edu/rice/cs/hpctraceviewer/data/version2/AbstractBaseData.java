package edu.rice.cs.hpctraceviewer.data.version2;

import java.io.IOException;
import java.util.List;

import edu.rice.cs.hpc.data.db.IdTuple;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.IFileDB;
import edu.rice.cs.hpc.data.experiment.extdata.IFileDB.IdTupleOption;
import edu.rice.cs.hpc.data.util.Constants;

/*********************************************************
 * 
 * Abstract class to manage trace data. 
 * This class is the parent for all regular data and filtered data
 *
 *********************************************************/
public abstract class AbstractBaseData implements IBaseData 
{
	final protected IFileDB baseDataFile;

	public AbstractBaseData(IFileDB baseDataFile){
		this.baseDataFile = baseDataFile;
	}
	
	
	@Override
	public List<IdTuple> getListOfIdTuples(IdTupleOption option) {
		return baseDataFile.getIdTuple(option);
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


	@Override
	public boolean isHybridRank() {
		return baseDataFile.getParallelismLevel() > 1;
	}

	@Override
	public int getNumLevels() {
		return baseDataFile.getParallelismLevel();
	}

	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#dispose()
	 */
	@Override
	public void dispose() {
		this.baseDataFile.dispose();
	}
	

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getRecordSize()
	 */
	@Override
	public int getRecordSize() {
		return Constants.SIZEOF_INT + Constants.SIZEOF_LONG;
	}

}
