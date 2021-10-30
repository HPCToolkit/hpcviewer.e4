package edu.rice.cs.hpcdata.experiment;

import java.util.List;

import edu.rice.cs.hpcdata.experiment.scope.Scope;

public interface IExperiment {

	
	public void setRootScope(Scope rootScope);
	public Scope getRootScope();
		
	public List<?> getRootScopeChildren();
	
	public IExperiment duplicate();
	
}
