package edu.rice.cs.hpctraceviewer.data.timeline;

public class ProcessTimelineService 
{
	private ProcessTimeline[] traces;

	public void setProcessTimeline(ProcessTimeline[] traces) {
		this.traces = traces;
	}
	
	public boolean setProcessTimeline(int index, ProcessTimeline trace) {
		if (traces != null && traces.length > index) {
			traces[index] = trace;
			return true;
		}
		return false;
	}
	
	
	public ProcessTimeline getProcessTimeline(int proc) {		
		if (traces != null && proc >= 0 && proc < traces.length)
			return traces[proc];
		
		return null;
	}
	
	
	public int getNumProcessTimeline() {
		if (traces == null)
			return 0;
		return traces.length;
	}
	
	
	public boolean isFilled() {
		if (traces != null) {
			for (ProcessTimeline trace: traces) {
				if (trace == null)
					return false;
			}
			return true;
		}
		return false;
	}

	
	public void dispose() {
		if (traces == null)
			return;
		
		for(var trace: traces) {
			trace.dispose();
		}
		traces = null;
	}
}
