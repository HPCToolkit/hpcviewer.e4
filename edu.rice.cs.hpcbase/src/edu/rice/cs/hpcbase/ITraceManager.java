package edu.rice.cs.hpcbase;

import edu.rice.cs.hpcdata.experiment.extdata.IBaseData;

public interface ITraceManager 
{
	IProcessTimeline getCurrentSelectedTraceline();
	
	
	IBaseData getTraceData();
	
	
	boolean hasTraces();
}