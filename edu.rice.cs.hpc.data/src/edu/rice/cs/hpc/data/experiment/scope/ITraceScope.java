package edu.rice.cs.hpc.data.experiment.scope;

import java.util.Vector;

public interface ITraceScope 
{
	public void setDepth(int depth);
	public int  getDepth();
	
	public Scope getScopeAt(int depth);
	public Vector<String> getFunctionNames();
}
