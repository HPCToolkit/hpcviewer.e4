package edu.rice.cs.hpc.data.trace;

public class TraceAttribute extends BaseTraceAttribute
{
	
	public String dbGlob;
	public int dbHeaderSize;	
	
	public TraceAttribute() {
		dbGlob = null;
		dbHeaderSize = 0;
	}
}
