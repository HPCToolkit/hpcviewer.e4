package edu.rice.cs.hpcdata.experiment.scope;


public class RootScopeType 
{
	public static final RootScopeType Invisible = new RootScopeType("Invisible");
	public static final RootScopeType Flat = new RootScopeType("Flat");
	public static final RootScopeType CallingContextTree = new RootScopeType("CallingContextTree");
	public static final RootScopeType CallerTree = new RootScopeType("CallerTree");
	public static final RootScopeType DatacentricTree = new RootScopeType("Datacentric");
	public static final RootScopeType Unknown         = new RootScopeType("Unknown");
	
	public String toString() { return value; }
	
	private String value;
	protected RootScopeType(String value) { this.value = value; };
}