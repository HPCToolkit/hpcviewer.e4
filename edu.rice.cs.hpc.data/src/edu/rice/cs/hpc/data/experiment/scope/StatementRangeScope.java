//////////////////////////////////////////////////////////////////////////
//																		//
//	StatementRangeScope.java											//
//																		//
//	experiment.scope.StatementRangeScope -- a range of statements		//
//	Last edited: August 10, 2001 at 2:22 pm								//
//																		//
//	(c) Copyright 2001 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.scope;


import edu.rice.cs.hpc.data.experiment.scope.visitors.IScopeVisitor;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;




//////////////////////////////////////////////////////////////////////////
//	CLASS STATEMENT-RANGE-SCOPE											//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * A scope in an HPCView experiment consisting of a range of lines.
 *
 */


public class StatementRangeScope extends Scope
{




//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a StatementRangeScope.
 ************************************************************************/
	
public StatementRangeScope(RootScope root, SourceFile file, int first, int last, int cct_id, int flat_id)
{
	super(root, file, first, last, cct_id, flat_id);
}

/*
public StatementRangeScope(Experiment experiment, SourceFile file, int first, int last)
{
	super(experiment, file, first, last, Scope.idMax++);
}
*/

//////////////////////////////////////////////////////////////////////////
//	SCOPE DISPLAY														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the user visible name for this statement-range scope.
 ************************************************************************/
	
public String getName()
{
	return this.getSourceCitation();
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
	return this.getLineNumberCitation();
}


/*************************************************************************
 *	Return a duplicate of this statement range scope, 
 *  minus the tree information .
 ************************************************************************/

public Scope duplicate() {
    StatementRangeScope duplicatedScope = 
	new StatementRangeScope(this.root, 
				this.sourceFile, 
				this.firstLineNumber,
				this.lastLineNumber,
				getCCTIndex(), this.flat_node_index);
    return duplicatedScope;
}


//////////////////////////////////////////////////////////////////////////
//support for visitors													//
//////////////////////////////////////////////////////////////////////////

public void accept(IScopeVisitor visitor, ScopeVisitType vt) {
	visitor.visit(this, vt);
}


}








