package edu.rice.cs.hpc.data.experiment.scope;

/* Java 1.5 public enum CallSiteScopeType { CALL_FROM_PROCEDURE, CALL_TO_PROCEDURE }*/

// Java 1.4 Compatible enumeration type
public class CallSiteScopeType {
	public final static CallSiteScopeType CALL_FROM_PROCEDURE = new CallSiteScopeType("FROM");
	public final static CallSiteScopeType CALL_TO_PROCEDURE = new CallSiteScopeType("TO");
	public String toString() { return value; }
	
	private String value;
	private CallSiteScopeType(String value) { this.value = value; };
}