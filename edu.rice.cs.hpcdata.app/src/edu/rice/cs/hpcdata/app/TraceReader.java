// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcdata.app;

import java.io.IOException;

import edu.rice.cs.hpcdata.db.IFileDB;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.trace.TraceRecord;
import edu.rice.cs.hpcdata.util.Constants;

public class TraceReader 
{
	public static final int RECORD_SIZE = Constants.SIZEOF_LONG + Constants.SIZEOF_INT;
	
	private final IFileDB data;
	
	public TraceReader(IFileDB data) {
		this.data = data;
	}
	
	public TraceRecord getData(long location) throws IOException
	{
		final long time = data.getLong(location);
		final int cpId = data.getInt(location + Constants.SIZEOF_LONG);
		
		return new TraceRecord(time, cpId);
	}
	
	public TraceRecord getData(IdTuple rank, long relativeIndex) throws IOException 
	{
		long location = getAbsoluteLocation(rank, relativeIndex);
		return getData(location);
	}
	
	public long getNumberOfRecords(IdTuple rank)
	{
		long start = data.getMinLoc(rank);
		long end   = data.getMaxLoc(rank);
		
		return getNumberOfRecords(start, end);
	}
	
	public long getNumberOfRecords(long start, long end)
	{
		return (end-start) / (getRecordSize());
	}

	public int getRecordSize() {
		return Constants.SIZEOF_INT + Constants.SIZEOF_LONG;
	}
	
	public long getAbsoluteLocation(IdTuple rank, long relativePosition)
	{
		return data.getMinLoc(rank) + (relativePosition * getRecordSize());
	}
	
	public long getRelativeLocation(IdTuple rank, long absolutePosition)
	{
		return (absolutePosition-data.getMinLoc(rank)) / getRecordSize();
	}
	

}
