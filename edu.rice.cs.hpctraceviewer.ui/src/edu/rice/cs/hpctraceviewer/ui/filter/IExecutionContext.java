package edu.rice.cs.hpctraceviewer.ui.filter;

import edu.rice.cs.hpcdata.db.IdTuple;

public interface IExecutionContext 
{
	IdTuple getIdTuple();
	
	int getNumSamples();
}
