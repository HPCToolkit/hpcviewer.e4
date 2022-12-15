//////////////////////////////////////////////////////////////////////////
//									//
//	ProcedureScope.java						//
//									//
//	experiment.scope.ProcedureScope -- a procedure scope		//
//	Last edited: August 10, 2001 at 2:22 pm				//
//									//
//	(c) Copyright 2002-2022 Rice University. All rights reserved.	//
//									//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpcdata.experiment.scope;

import java.util.List;

import edu.rice.cs.hpcdata.experiment.scope.filters.MetricValuePropagationFilter;
import edu.rice.cs.hpcdata.experiment.scope.visitors.IScopeVisitor;
import edu.rice.cs.hpcdata.experiment.source.SourceFile;
import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpcdata.util.IUserData;




//////////////////////////////////////////////////////////////////////////
//	CLASS PROCEDURE-SCOPE						//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * A procedure scope in an HPCView experiment.
 *
 */


public class ProcedureScope extends Scope  implements IMergedScope 
{
	public enum ProcedureType {REGULAR, INLINE_FUNCTION, INLINE_MACRO, ENTRY_POINT}
	
	public static final int FEATURE_PROCEDURE    = 0;
	public static final int FEATURE_PLACE_HOLDER = 1;
	public static final int FEATURE_ROOT 	     = 2;
	public static final int FEATURE_ELIDED       = 3;
	public static final int FEATURE_TOPDOWN      = 4;
	
	public static final String INLINE_NOTATION = "[I] ";
	
	public static final ProcedureScope NONE = new ProcedureScope(null, 
			LoadModuleScope.NONE, SourceFile.NONE, 
			0, 0, 
			Constants.PROCEDURE_UNKNOWN, ProcedureType.REGULAR, 
			Constants.FLAT_ID_PROC_UNKNOWN, Constants.FLAT_ID_PROC_UNKNOWN, 
			null, FEATURE_ROOT);

	private static final String PROCEDURE_NO_NAME = "-";
	private static final String PROCEDURE_INLINE  = "<inline>";

	private final ProcedureType procedureType;
	private final int procedureFeature;
	
	/** The name of the procedure. */
	protected String procedureName;
	
	// we assume that all procedure scope has the information on load module it resides
	protected LoadModuleScope objLoadModule;


//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION	
//////////////////////////////////////////////////////////////////////////


	
/***
 *	Creates a ProcedureScope.
 * 
 * @param root
 * 			the root scope
 * @param loadModule
 * 			The load module
 * @param file
 * 			The file of this procedure
 * @param first
 * 			start line number
 * @param last
 * 			last line number
 * @param proc
 * 			The name of the procedure
 * @param procedureType
 * 			Type of the procedure (inline, regular, ...)
 * @param cct_id
 * 			unique id
 * @param flat_id
 * 			static id of the procedure. Used to create flat view
 * @param userData
 * 			User's defined name
 * @param procedureFeature 
 * 			{@code int}
 * 			kind of procedure: FeatureProcedure, FeaturePlaceHolder, 
 * 			FeatureRoot, FeatureElided and FeatureTopDown
 * 
 * @apiNote procedureFeature is for backward compatibility only, used by the old database
 * 
 * @see ProcedureType
 */
public ProcedureScope(RootScope root, LoadModuleScope loadModule, SourceFile file, 
		int first, int last, String proc, ProcedureType procedureType, int cct_id, int flat_id, 
		IUserData<String,String> userData, int procedureFeature)
{
	super(root, file, first, last, cct_id, flat_id);
	
	this.procedureType = procedureType;
	this.procedureName = proc;

	if (userData != null) {
		String newName = userData.get(proc);
		if (newName != null) 
			procedureName = newName;
	}
	// prefix the inline functions and macros with inline notation
	// either "inlined from .. " or "[I]"  
	if (procedureType == ProcedureType.INLINE_FUNCTION ||
		procedureType == ProcedureType.INLINE_MACRO) {
		
		if (procedureName.isEmpty() || 
			procedureName.equals(PROCEDURE_NO_NAME) || 
			procedureName.equals(PROCEDURE_INLINE)) {
			
			procedureName =  "inlined from " + getSourceCitation();
		}
		if (!procedureName.startsWith(INLINE_NOTATION))
			procedureName = INLINE_NOTATION + procedureName;
	}
	this.procedureFeature = procedureFeature;
	this.objLoadModule = loadModule;
}


//////////////////////////////////////////////////////////////////////////
//	SCOPE DISPLAY	
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the user visible name for this scope.
 ************************************************************************/
	
public String getName()
{	
	return procedureName;
}


/*************************************************************************
 *	Return a duplicate of this procedure scope, 
 *  minus the tree information .
 ************************************************************************/

public Scope duplicate() {
	return new ProcedureScope(this.root,
			this.objLoadModule,
			this.sourceFile, 
			this.firstLineNumber, 
			this.lastLineNumber,
			this.procedureName,
			this.procedureType,
			getCCTIndex(), // Laks 2008.08.26: add the sequence ID
			this.flat_node_index,
			null,
			this.procedureFeature);
}


/****
 * Retrieve the procedure type of this scope.
 * 
 * @see ProcedureType
 * 
 * @return ProcedureType
 */
public ProcedureType getProcedureType() {
	return procedureType;
}


/****
 * Return true of the scope is an inlined function,
 * false otherwise.
 * 
 * @apiNote Only to support for backward compatibility
 * 
 * @return boolean
 */
public boolean isAlien() {
	return procedureType == ProcedureType.INLINE_FUNCTION ||
		   procedureType == ProcedureType.INLINE_MACRO;
}

//////////////////////////////////////////////////////////////////////////
//support for visitors													//
//////////////////////////////////////////////////////////////////////////

@Override
public void accept(IScopeVisitor visitor, ScopeVisitType vt) {
	visitor.visit(this, vt);
}

//////////////////////////////////////////////////////////////////////////
//support for Flat visitors												//
//////////////////////////////////////////////////////////////////////////

public LoadModuleScope getLoadModule() {
	return this.objLoadModule;
}


//////////////////////////////////////////////////////////////////////////
//support for bottom-up visitors										//
//////////////////////////////////////////////////////////////////////////

@Override
public List<Scope> getAllChildren(/*AbstractFinalizeMetricVisitor finalizeVisitor, PercentScopeVisitor percentVisitor,*/
		MetricValuePropagationFilter inclusiveOnly,
		MetricValuePropagationFilter exclusiveOnly) 
{
	return this.getChildren();
}


//////////////////////////////////////////////////////////////////////////
// misc
//////////////////////////////////////////////////////////////////////////

public void setLoadModule(LoadModuleScope lm) {
	this.objLoadModule = lm;
}


public boolean isTopDownProcedure() 
{	
	return procedureFeature == FEATURE_TOPDOWN;
}

public boolean isFalseProcedure()
{
	return procedureFeature != FEATURE_PROCEDURE;
}

public boolean toBeElided() 
{
	return procedureFeature == FEATURE_ELIDED;
}

@Override
public boolean hasScopeChildren() {
	return node.hasChildren();
}
}


