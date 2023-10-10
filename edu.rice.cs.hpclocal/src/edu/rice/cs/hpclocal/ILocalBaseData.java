package edu.rice.cs.hpclocal;

import java.io.IOException;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.experiment.extdata.IBaseData;

public interface ILocalBaseData extends IBaseData 
{

	long getLong(long position) throws IOException;
	
	int getInt(long position) throws IOException;
	
	long getMinLoc(IdTuple profile);
	
	long getMaxLoc(IdTuple profile);
	
	int getRecordSize();
}
