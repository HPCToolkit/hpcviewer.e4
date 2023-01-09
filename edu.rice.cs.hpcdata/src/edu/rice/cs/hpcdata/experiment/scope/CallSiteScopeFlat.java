package edu.rice.cs.hpcdata.experiment.scope;

public class CallSiteScopeFlat extends CallSiteScope 
{
	private boolean procHasChildren;
	
	public CallSiteScopeFlat(CallSiteScope cctScope) {
		super((LineScope) cctScope.getLineScope().duplicate(), 
			  (ProcedureScope) cctScope.getProcedureScope().duplicate(), 
			  cctScope.getType(), 
			  cctScope.getCCTIndex(), 
			  cctScope.getFlatIndex());
		
		procHasChildren = cctScope.hasChildren();
	}

	
	/***
	 * Return true if the original associated CCT nodes at least one has children.
	 * 
	 * 
	 * @return {@code boolean}
	 */
	public boolean cctHasChildren() {
		return procHasChildren;
	}
	
	
	public void combine(Scope sourceCCT) {
		procHasChildren = procHasChildren || sourceCCT.hasChildren();
	}
	
	
	public ProcedureScope getFlatProcedureScope() {
		return procScope;
	}
	
	public void setFlatProcedureScope(ProcedureScope proc) {
		this.procScope = proc;
	}
}
