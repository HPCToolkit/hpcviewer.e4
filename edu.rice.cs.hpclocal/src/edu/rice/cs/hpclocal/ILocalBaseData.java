// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpclocal;

import java.io.IOException;

import edu.rice.cs.hpcbase.IBaseData;
import edu.rice.cs.hpcdata.db.IdTuple;


public interface ILocalBaseData extends IBaseData 
{

	long getLong(long position) throws IOException;
	
	int getInt(long position) throws IOException;
	
	long getMinLoc(IdTuple profile);
	
	long getMaxLoc(IdTuple profile);
	
	int getRecordSize();
}
