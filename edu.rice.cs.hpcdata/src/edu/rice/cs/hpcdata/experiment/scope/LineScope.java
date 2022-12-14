//////////////////////////////////////////////////////////////////////////
//																		//
//	LineScope.java														//
//																		//
//	experiment.scope.LineScope -- a single-line scope in an experiment	//
//	Last edited: May 18, 2001 at 6:19 pm								//
//																		//
//	(c) Copyright 2002-2022 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpcdata.experiment.scope;

import edu.rice.cs.hpcdata.experiment.scope.visitors.IScopeVisitor;
import edu.rice.cs.hpcdata.experiment.source.SourceFile;




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
}


//////////////////////////////////////////////////////////////////////////
//	SCOPE DISPLAY														//
//////////////////////////////////////////////////////////////////////////


/*************************************************************************
 *	Returns the user visible name for this loop scope.
 ************************************************************************/
	
public String getName()
{
	if (this.sourceFile==null) {
		// fix issue with tooltip: need to add 1 to the line number to
		// match with the call site label (added 1 in CallSiteArrowPainter class).
		// In the future we need to deal whether the line number is based on zero or one.
		// otherwise we keep adding one everywhere.
		return "unknown file:" + (1+this.lastLineNumber);
	}
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
	return new LineScope(this.root, 
				this.sourceFile, 
				this.firstLineNumber,
				getCCTIndex(), this.flat_node_index);
}

//////////////////////////////////////////////////////////////////////////
//support for visitors													//
//////////////////////////////////////////////////////////////////////////

@Override
public void accept(IScopeVisitor visitor, ScopeVisitType vt) {
	visitor.visit(this, vt);
}

}








