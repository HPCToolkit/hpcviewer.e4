package edu.rice.cs.hpctraceviewer.data.version2;

import java.io.IOException;
import java.util.List;

import edu.rice.cs.hpcdata.db.IFileDB;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.experiment.extdata.IBaseData;
import edu.rice.cs.hpcdata.util.Constants;

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


	@Override
	public void setListOfIdTuples(List<IdTuple> listIdTuples) {
		// Should we update the list of id tuples here?
		// this method is designed tp replace the existing id tuples
		// which doesn't make any sense for the database to be
		// modified.
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

	@Override
	public boolean hasGPU() {
		return baseDataFile.hasGPU();
	}
}
