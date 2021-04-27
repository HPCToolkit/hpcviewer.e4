//////////////////////////////////////////////////////////////////////////
//									//
//	LoadModuleScope.java						//
//									//
//	experiment.scope.LoadModuleScope -- a load module scope		//
//	Last edited: April 4, 2003 at 5:00 pm				//
//									//
//	(c) Copyright 2003 Rice University. All rights reserved.	//
//									//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpcdata.experiment.scope;


import edu.rice.cs.hpcdata.experiment.scope.visitors.IScopeVisitor;
import edu.rice.cs.hpcdata.experiment.source.SourceFile;




//////////////////////////////////////////////////////////////////////////
//	CLASS LOADMODULE-SCOPE						//
//////////////////////////////////////////////////////////////////////////

/*
 *
 * A load module scope in an HPCView experiment.
 *
 */


public class LoadModuleScope extends Scope
{

/** The name of the load module. */
protected String loadModuleName;




//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION							//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a LoadModuleScope.
 ************************************************************************/
	
public LoadModuleScope(RootScope root, String lmname, SourceFile file, int id)
{
	super(root, file, id);
	this.loadModuleName = lmname;
}



public Scope duplicate() {
    return new LoadModuleScope(this.root, this.loadModuleName, this.sourceFile, this.flat_node_index);
}


/*****
 * Create a new load module with pre-build id
 * 
 * @param root
 * @param name
 * @param file
 * @return
 */
static public LoadModuleScope build(RootScope root, String name, SourceFile file) {
	id--;
	
	return new LoadModuleScope(root, name, file, id);
}

/*
public int hashCode() {
	return this.loadModuleName.hashCode();
} */

//////////////////////////////////////////////////////////////////////////
//	SCOPE DISPLAY														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the user visible name for this scope.
 ************************************************************************/
	
public String getName()
{
    return loadModuleName;
}

//////////////////////////////////////////////////////////////////////////
//support for visitors													//
//////////////////////////////////////////////////////////////////////////

public void accept(IScopeVisitor visitor, ScopeVisitType vt) {
	visitor.visit(this, vt);
}

/**
 * retrieve the original name of the module
 * @return
 */
public String getModuleName() {
	return this.loadModuleName;
}

/**
 * Load module doesn't have source file, so it needs to return its name for the citation
 * @return the citation
 */
protected String getSourceCitation()
{
	return getName();  
}

}
