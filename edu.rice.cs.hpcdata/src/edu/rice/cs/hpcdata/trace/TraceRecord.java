package edu.rice.cs.hpcdata.trace;

public class TraceRecord 
{
	public long timestamp;
	public int cpId;

	public TraceRecord(long timestamp, int cpid) {
		this.timestamp = timestamp;
		this.cpId	   = cpid;
	}
 }
