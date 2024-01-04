package edu.rice.cs.hpctraceviewer.ui.base;

import edu.rice.cs.hpcbase.ui.IMainPart;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;

public interface ITracePart extends IMainPart, ITraceContext 
{
	void activateStatisticItem();
	
	SpaceTimeDataController getDataController();
}
