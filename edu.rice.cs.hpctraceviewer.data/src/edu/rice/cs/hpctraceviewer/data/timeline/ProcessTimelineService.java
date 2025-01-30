// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.data.timeline;

import java.util.Objects;
import java.util.stream.Stream;

import edu.rice.cs.hpcbase.IProcessTimeline;

public class ProcessTimelineService 
{
	private IProcessTimeline[] traces;

	public void setProcessTimeline(IProcessTimeline[] traces) {
		this.traces = traces;
	}
	
	public boolean setProcessTimeline(int index, IProcessTimeline trace) {
		if (traces != null && traces.length > index) {
			traces[index] = trace;
			return true;
		}
		return false;
	}
	
	
	public IProcessTimeline getProcessTimeline(int proc) {		
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
			boolean hasNullTrace = Stream.of(traces).anyMatch(Objects::isNull);
			return !hasNullTrace;
		}
		return false;
	}

	
	public void dispose() {
		if (traces == null)
			return;
		
		for(var trace: traces) {
			if (trace != null)
				trace.dispose();
		}
		traces = null;
	}
}
