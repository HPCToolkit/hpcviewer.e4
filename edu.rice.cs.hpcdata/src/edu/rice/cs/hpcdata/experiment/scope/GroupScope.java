//////////////////////////////////////////////////////////////////////////
//																		//
//	GroupScope.java														//
//																		//
//	experiment.scope.GroupScope -- a scope with arbitrary scope			//
//									types as children					//
//	Last edited: February 8, 2005 										//
//																		//
//	(c) Copyright 2002-2022 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpcdata.experiment.scope;

import edu.rice.cs.hpcdata.experiment.scope.visitors.IScopeVisitor;




//////////////////////////////////////////////////////////////////////////
//	CLASS GROUP-SCOPE													//
//////////////////////////////////////////////////////////////////////////

/*
 *
 * A group scope in an HPCView experiment.
 *
 */


public class GroupScope extends Scope
{


/** The name of the group scope. */
protected String groupName;




//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a GroupScope.
 ************************************************************************/
	
public GroupScope(RootScope root, String groupname)
{
	// the ID needs to be defined further
	super(root, null, Scope.NO_LINE_NUMBER, Scope.NO_LINE_NUMBER, -1, -1);
	this.groupName = groupname;
//	this.id = "GroupScope";
}


public Scope duplicate() {
    return new GroupScope(this.root, this.groupName);
}

//////////////////////////////////////////////////////////////////////////
//	SCOPE DISPLAY														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the user visible name for this scope.
 ************************************************************************/
	
public String getName()
{
	return "Group " +  this.groupName;
}

//////////////////////////////////////////////////////////////////////////
//support for visitors													//
//////////////////////////////////////////////////////////////////////////

public void accept(IScopeVisitor visitor, ScopeVisitType vt) {
	visitor.visit(this, vt);
}


}
