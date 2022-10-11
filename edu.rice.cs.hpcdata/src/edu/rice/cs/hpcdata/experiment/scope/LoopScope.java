//////////////////////////////////////////////////////////////////////////
//																		//
//	LoopScope.java														//
//																		//
//	experiment.scope.LoopScope -- a loop scope							//
//	Last edited: August 10, 2001 at 2:22 pm								//
//																		//
//	(c) Copyright 2002-2022 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpcdata.experiment.scope;


import edu.rice.cs.hpcdata.experiment.scope.visitors.IScopeVisitor;
import edu.rice.cs.hpcdata.experiment.source.SourceFile;




//////////////////////////////////////////////////////////////////////////
//	CLASS LOOP-SCOPE													//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * A loop scope in an HPCView experiment.
 *
 */


public class LoopScope extends Scope
{




//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a LoopScope.
 ************************************************************************/
	
public LoopScope(RootScope root, SourceFile file, int first, int last, int cct_id, int flat_id)
{
	super(root, file, first, last, cct_id, flat_id);
}


public Scope duplicate() {
    return new LoopScope(this.root,  this.sourceFile,  
    		this.firstLineNumber,  this.lastLineNumber, getCCTIndex(), this.flat_node_index);
}


//////////////////////////////////////////////////////////////////////////
//	SCOPE DISPLAY														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the user visible name for this scope.
 ************************************************************************/
	
public String getName()
{
	final String PREFIX_LOOP = "loop at ";
	
	if (getSourceFile() == null)
		return PREFIX_LOOP + SourceFile.NONE.getName();
	return PREFIX_LOOP + this.getSourceCitation();
}




/*************************************************************************
 *	Returns the short user visible name for this scope.
 *
 *	This name is only used in tree views where the scope's name appears
 *	in context with its containing scope's name.
 *
 *	Subclasses may override this to implement better short names.
 *
 ************************************************************************/
	
public String getShortName()
{
	return "loop at " + this.getLineNumberCitation();
}


//////////////////////////////////////////////////////////////////////////
//support for visitors													//
//////////////////////////////////////////////////////////////////////////

public void accept(IScopeVisitor visitor, ScopeVisitType vt) {
	visitor.visit(this, vt);
}


}








