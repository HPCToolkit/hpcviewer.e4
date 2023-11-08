package edu.rice.cs.hpctraceviewer.ui.debug;

import edu.rice.cs.hpcbase.IProcessTimeline;


public interface IProcessTimelineSource 
{
	IProcessTimeline getCurrentProcessTimeline();
}
