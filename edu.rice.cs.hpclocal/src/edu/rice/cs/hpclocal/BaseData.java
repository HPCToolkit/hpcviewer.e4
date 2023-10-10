package edu.rice.cs.hpclocal;

import edu.rice.cs.hpcdata.db.IFileDB;
import edu.rice.cs.hpcdata.db.IdTuple;


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
	

	@Override
	public int getNumberOfRanks() {
		return baseDataFile.getNumberOfRanks();
	}
	

	@Override
	public long getMinLoc(IdTuple profile) {
		return baseDataFile.getMinLoc(profile);
	}

	@Override
	public long getMaxLoc(IdTuple profile) {
		return baseDataFile.getMaxLoc(profile);
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
