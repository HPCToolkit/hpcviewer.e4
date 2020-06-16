package edu.rice.cs.hpcviewer.ui.parts.thread;

import java.util.List;

import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;

public class ThreadViewInput 
{
	final private RootScope rootScope; 
	final private IThreadDataCollection threadData;
	final private List<Integer> threads;
	
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
}
