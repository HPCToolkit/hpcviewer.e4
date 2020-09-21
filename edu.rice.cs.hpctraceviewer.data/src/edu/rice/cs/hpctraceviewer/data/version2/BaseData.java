package edu.rice.cs.hpctraceviewer.data.version2;

import edu.rice.cs.hpc.data.experiment.extdata.IFileDB;


/*************************************
 * 
 * basic implementation of IBaseData
 *
 *************************************/
public class BaseData extends AbstractBaseData {
	
	public BaseData(IFileDB baseDataFile) 
	{
		super(baseDataFile);
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getListOfRanks()
	 */
	public String[] getListOfRanks() {
		return baseDataFile.getRankLabels();
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getNumberOfRanks()
	 */
	public int getNumberOfRanks() {
		return baseDataFile.getNumberOfRanks();
	}
	

	@Override
	public long getMinLoc(int rank) {
		return baseDataFile.getMinLoc(rank);
	}

	@Override
	public long getMaxLoc(int rank) {
		return baseDataFile.getMaxLoc(rank);
	}

	@Override
	public int getFirstIncluded() {
		return 0;
	}

	@Override
	public int getLastIncluded() {
		return baseDataFile.getNumberOfRanks()-1;
	}

	@Override
	public boolean isDenseBetweenFirstAndLast() {
		return true;//No filtering
	}
}
