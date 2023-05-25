package edu.rice.cs.hpcbase;

import java.util.List;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;

public class ThreadViewInput 
{
	private final IThreadDataCollection threadData;
	private RootScope rootScope; 
	private List<IdTuple> threads;
	
	public ThreadViewInput(RootScope rootScope, IThreadDataCollection threadData, List<IdTuple> idTuples) {
		this.rootScope  = rootScope;
		this.threadData = threadData;
		this.threads    = idTuples;
	}
	
	public RootScope getRootScope() {
		return rootScope;
	}
	
	public void setRootScope(RootScope root) {
		this.rootScope = root;
	}

	public IThreadDataCollection getThreadData() {
		return threadData;
	}

	public List<IdTuple> getThreads() {
		return threads;
	}
	
	public void setThread(List<IdTuple> threads) {
		this.threads = threads;
	}
	
	public String toString() {
		var exp = rootScope.getExperiment();
		int dbId = exp.getPath().hashCode();
		return exp.toString() + ":" + String.valueOf(dbId);
	}
}
