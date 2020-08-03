package edu.rice.cs.hpctraceviewer.ui.base;

import edu.rice.cs.hpcbase.ui.IMainPart;

public interface ITracePart extends IMainPart, ITraceContext 
{
	public void activateStatisticItem();
	
	public Object getInput();
}
