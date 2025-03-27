// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcbase;

import java.util.List;

import org.hpctoolkit.db.local.db.IdTuple;
import org.hpctoolkit.db.local.experiment.extdata.IThreadDataCollection;
import org.hpctoolkit.db.local.experiment.scope.RootScope;

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
	
	
	@Override
	public String toString() {
		var exp = rootScope.getExperiment();
		int dbId = exp.getDirectory().hashCode();
		return exp.toString() + ":" + String.valueOf(dbId);
	}
}
