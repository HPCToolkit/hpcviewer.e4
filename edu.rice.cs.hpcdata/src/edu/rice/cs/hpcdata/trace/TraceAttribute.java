package edu.rice.cs.hpcdata.trace;

public class TraceAttribute extends BaseTraceAttribute
{
	
	public String dbGlob;
	public int dbHeaderSize;	
	
	public TraceAttribute() {
		dbGlob = null;
		dbHeaderSize = 0;
	}
}
