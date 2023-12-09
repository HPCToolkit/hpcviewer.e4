package edu.rice.cs.hpcbase;

import org.eclipse.core.runtime.IProgressMonitor;

import edu.rice.cs.hpcdata.util.IProgressReport;


/**************************
 * 
 * Wrapper for Eclipse's {@code IProgressMonitor} to conform with
 * {@code IProgressReport} interface.
 *
 **************************/
public class ProgressReport implements IProgressReport 
{
	private static final int NUM_STEPS = 10;
	private final IProgressMonitor progressMonitor;
	
	private int workDone;
	private int workStep;
	
	public ProgressReport(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
		
		// in case the programmer is stupid enough to start the job without calling begin
		workDone = 0;
		workStep = NUM_STEPS;
	}
	
	
	@Override
	public void begin(String title, int numTasks) {
		progressMonitor.beginTask(title, NUM_STEPS);
		
		workDone = 0;
		
		int work = numTasks <= 2 ? 2 : numTasks;
		workStep = work / NUM_STEPS;
	}

	@Override
	public void advance() {
		workDone++;
		if (workDone % workStep == 0)
			progressMonitor.worked(1);
	}

	@Override
	public void end() {
		progressMonitor.done();
	}

}
