package edu.rice.cs.hpcdata.db.version4;

import edu.rice.cs.hpcdata.experiment.scope.Scope;

public class ScopeContext 
{
	public long szChildren;
	public long pChildren;
	public int  ctxId;
	public byte nFlexWords;
	public short propagation;
	
	public Scope newScope;
}
