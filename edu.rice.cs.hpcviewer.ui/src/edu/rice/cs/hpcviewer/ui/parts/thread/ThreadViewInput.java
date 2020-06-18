package edu.rice.cs.hpcviewer.ui.parts.thread;

import java.util.List;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;

public class ThreadViewInput 
{
	final private RootScope rootScope; 
	final private IThreadDataCollection threadData;
	private List<Integer> threads;
	
	public ThreadViewInput(RootScope rootScope, IThreadDataCollection threadData, List<Integer> threads) {
		this.rootScope  = rootScope;
		this.threadData = threadData;
		this.threads    = threads;
	}
	
	public RootScope getRootScope() {
		return rootScope;
	}

	public IThreadDataCollection getThreadData() {
		return threadData;
	}

	public List<Integer> getThreads() {
		return threads;
	}
	
	public void setThread(List<Integer> threads) {
		this.threads = threads;
	}
	
	public String toString() {
		BaseExperiment exp = rootScope.getExperiment();
		int dbId = exp.getDefaultDirectory().getAbsolutePath().hashCode();
		String str = exp.getName() + ":" + String.valueOf(dbId);
		
		return str;
	}
}
