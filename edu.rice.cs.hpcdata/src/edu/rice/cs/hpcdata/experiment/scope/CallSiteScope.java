//////////////////////////////////////////////////////////////////////////
//									//
//	CallSiteScope.java				//
//									//
//	experiment.scope.CallSiteScope -- a function callsite scope	//
//	(c) Copyright 2001-2012 Rice University. All rights reserved.	//
//  $Id$							//
//									//
//////////////////////////////////////////////////////////////////////////
package edu.rice.cs.hpcdata.experiment.scope;

import edu.rice.cs.hpcdata.experiment.scope.visitors.IScopeVisitor;


//////////////////////////////////////////////////////////////////////////
//	CLASS CALLSITE-SCOPE                                                //
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * A callsite scope in an CSProf experiment.
 *
 */


public class CallSiteScope extends Scope
{


protected LineScope lineScope;

protected ProcedureScope procScope;

protected CallSiteScopeType type;


//////////////////////////////////////////////////////////////////////////
//INITIALIZATION	
//////////////////////////////////////////////////////////////////////////

public CallSiteScope(LineScope scope, ProcedureScope scope2, 
		CallSiteScopeType csst, int cct_id, int flat_id) 
{
	super(scope2.root, scope2.sourceFile,scope2.firstLineNumber,
			scope2.lastLineNumber, cct_id, flat_id);
	this.lineScope = scope;
	this.procScope = scope2;
	this.type = csst;
}

public Scope duplicate() {
    return new CallSiteScope(
    		(LineScope) lineScope.duplicate(), 
    		(ProcedureScope) procScope.duplicate(), 
    		type, getCCTIndex(), this.flat_node_index);
}


//////////////////////////////////////////////////////////////////////////
//SCOPE DISPLAY	
//////////////////////////////////////////////////////////////////////////


/*************************************************************************
 *	Returns the user visible name for this scope.
 ************************************************************************/
	
public String getName()
{
	return procScope.getName();
}

public ProcedureScope getProcedureScope()
{
	return this.procScope;
}

public LineScope getLineScope()
{
	return this.lineScope;
}


//////////////////////////////////////////////////////////////////////////
//support for visitors													//
//////////////////////////////////////////////////////////////////////////

public void accept(IScopeVisitor visitor, ScopeVisitType vt) {
	visitor.visit(this, vt);
}

public CallSiteScopeType getType() {
	return this.type;
}

}


