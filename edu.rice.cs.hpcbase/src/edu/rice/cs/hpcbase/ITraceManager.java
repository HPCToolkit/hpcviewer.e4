package edu.rice.cs.hpcbase;

public interface ITraceManager 
{
	IProcessTimeline getCurrentSelectedTraceline();
	
	
	boolean hasTraces();
}