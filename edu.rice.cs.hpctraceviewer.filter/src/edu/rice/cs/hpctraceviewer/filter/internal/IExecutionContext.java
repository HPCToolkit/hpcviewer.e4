package edu.rice.cs.hpctraceviewer.filter.internal;

import edu.rice.cs.hpcdata.db.IdTuple;

public interface IExecutionContext 
{
	IdTuple getIdTuple();
	
	int getNumSamples();
}
