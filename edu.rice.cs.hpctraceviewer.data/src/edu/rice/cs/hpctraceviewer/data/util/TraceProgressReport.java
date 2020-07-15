package edu.rice.cs.hpctraceviewer.data.util;

import org.eclipse.core.runtime.IProgressMonitor;
import edu.rice.cs.hpc.data.util.IProgressReport;

public class TraceProgressReport implements IProgressReport 
{
	final private IProgressMonitor progress;
	
	public TraceProgressReport(IProgressMonitor progress )
	{
		this.progress = progress;
	}
	
	public void begin(String title, int num_tasks) {
		progress.setTaskName(title);
		progress.beginTask("Starting: "+title, num_tasks);
	}

	public void advance() {
		progress.worked(1);
	}

	public void end() {
		progress.done();
	}	
}