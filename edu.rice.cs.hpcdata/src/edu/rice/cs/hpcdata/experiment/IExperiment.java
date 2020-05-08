package edu.rice.cs.hpcdata.experiment;

import edu.rice.cs.hpcdata.experiment.scope.Scope;

public interface IExperiment {

	
	public void setRootScope(Scope rootScope);
	public Scope getRootScope();
		
	public Object[] getRootScopeChildren();
	
	public IExperiment duplicate();
	
}
