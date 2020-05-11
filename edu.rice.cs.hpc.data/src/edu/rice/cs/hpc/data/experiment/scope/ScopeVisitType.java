package edu.rice.cs.hpc.data.experiment.scope;

/*public enum ScopeVisitType {
	PreVisit, PostVisit
}*/

/*public class ScopeVisitType {
	
	public final static int PreVisit = 0;
	public final static int PostVisit = 1;
	
	public int value;
	
	public ScopeVisitType(int v) { value = v; };
	public boolean isPreVisit() { return value == PreVisit; }
	public boolean isPostVisit() { return value == PostVisit; }
}*/

public class ScopeVisitType {
	
	public final static ScopeVisitType PreVisit = new ScopeVisitType("PRE");
	public final static ScopeVisitType PostVisit = new ScopeVisitType("POST");
	public String toString() { return value; }
	
	private String value;
	private ScopeVisitType(String value) { this.value = value; };
}