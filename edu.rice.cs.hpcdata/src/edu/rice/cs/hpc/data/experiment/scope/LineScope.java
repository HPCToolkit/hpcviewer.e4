//////////////////////////////////////////////////////////////////////////
//																		//
//	LineScope.java														//
//																		//
//	experiment.scope.LineScope -- a single-line scope in an experiment	//
//	Last edited: May 18, 2001 at 6:19 pm								//
//																		//
//	(c) Copyright 2001 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.scope;

import edu.rice.cs.hpc.data.experiment.scope.visitors.IScopeVisitor;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;




//////////////////////////////////////////////////////////////////////////
//	CLASS LINE-SCOPE													//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * A single-line scope in an HPCView experiment.
 *
 */


public class LineScope extends Scope
{




//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a LineScope.
 ************************************************************************/
	
public LineScope(RootScope root, SourceFile sourceFile, int lineNumber, int cct_id, int flat_id)
{
	super(root, sourceFile, lineNumber, lineNumber, cct_id, flat_id);
//	this.id = "LineScope";
}

/*
public LineScope(Experiment experiment, SourceFile sourceFile, int lineNumber)
{
	super(experiment, sourceFile, lineNumber, lineNumber, Scope.idMax++);
//	this.id = "LineScope";
}*/



//////////////////////////////////////////////////////////////////////////
//	SCOPE DISPLAY														//
//////////////////////////////////////////////////////////////////////////





/*************************************************************************
 *	Returns the user visible name for this loop scope.
 ************************************************************************/
	
public String getName()
{
	if (this.sourceFile==null) {
		return "unknown file:" + this.lastLineNumber;
	}
	return this.getSourceCitation();
}
/*
public int hashCode() {
	return this.getName().hashCode();
}
*/

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


public boolean isequal(LineScope ls)
{
	return ((this.firstLineNumber == ls.firstLineNumber) &&
		(this.lastLineNumber == ls.lastLineNumber) &&
		(this.sourceFile == ls.sourceFile));
}

//////////////////////////////////////////////////////////////////////////
//	ACCESS TO SCOPE														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the line number of this line scope.
 ************************************************************************/
	
public int getLineNumber()
{
	return this.firstLineNumber;
}


/*************************************************************************
 *	Return a duplicate of this line scope, 
 *  minus the tree information .
 ************************************************************************/

public Scope duplicate() {
	LineScope duplicatedScope = 
		new LineScope(this.root, 
				this.sourceFile, 
				this.firstLineNumber,
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








