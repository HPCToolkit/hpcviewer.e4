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
	public static final int FeatureProcedure   = 0;
	public static final int FeaturePlaceHolder = 1;
	public static final int FeatureRoot 	   = 2;
	public static final int FeatureElided      = 3;
	public static final int FeatureTopDown     = 4;
	
	public static final String INLINE_NOTATION = "[I] ";
	public static final ProcedureScope NONE = new ProcedureScope(null, 
			LoadModuleScope.NONE, SourceFile.NONE, 
			0, 0, 
			Constants.PROCEDURE_UNKNOWN, false, 
			Integer.MIN_VALUE, Integer.MIN_VALUE, 
			null, FeatureRoot);

	private static final String TheProcedureWhoShouldNotBeNamed = "-";
	private static final String TheInlineProcedureLabel 	 	= "<inline>";

	public static enum ProcedureType {
		ProcedureNormal, 
		ProcedureInlineFunction, 
		ProcedureInlineMacro, 
		ProcedureRoot,
		
		VariableDynamicAllocation, 
		VariableStatic, 
		VariableUnknown, 
		VariableAccess
	}

	
	final private int procedureFeature;
	
	private ProcedureType type;
	/** The name of the procedure. */
	protected String procedureName;
	protected boolean isalien;
	// we assume that all procedure scope has the information on load module it resides
	protected LoadModuleScope objLoadModule;


	/**
	 * scope ID of the procedure frame. The ID is given by hpcstruct and hpcprof
	 */
	//protected int iScopeID;

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
 * @param _isalien
 * 			boolean true if the procedure is inlined
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
 */
public ProcedureScope(RootScope root, LoadModuleScope loadModule, SourceFile file, 
		int first, int last, String proc, boolean _isalien, int cct_id, int flat_id, 
		IUserData<String,String> userData, int procedureFeature)
{
	super(root, file, first, last, cct_id, flat_id);
	
	this.isalien = _isalien;
	this.procedureName = proc;

	if (userData != null) {
		String newName = userData.get(proc);
		if (newName != null) 
			procedureName = newName;
	}
	if (isalien) {
		if (procedureName.isEmpty() || procedureName.equals(TheProcedureWhoShouldNotBeNamed)
				|| procedureName.equals(TheInlineProcedureLabel)) {
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
	ProcedureScope ps = new ProcedureScope(this.root,
			this.objLoadModule,
			this.sourceFile, 
			this.firstLineNumber, 
			this.lastLineNumber,
			this.procedureName,
			this.isalien,
			getCCTIndex(), // Laks 2008.08.26: add the sequence ID
			this.flat_node_index,
			null,
			this.procedureFeature);

	ps.setProcedureType(type);
	
	return ps;
}

public boolean isAlien() {
	return this.isalien;
}

//////////////////////////////////////////////////////////////////////////
//support for visitors													//
//////////////////////////////////////////////////////////////////////////

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
	return procedureFeature == FeatureTopDown;
}

public boolean isFalseProcedure()
{
	return procedureFeature != FeatureProcedure;
}

public boolean toBeElided() 
{
	return procedureFeature == FeatureElided;
}

public void setProcedureType(ProcedureType type) {
	this.type = type;
}

public ProcedureType getProcedureType() {
	return this.type;
}



@Override
public boolean hasScopeChildren() {
	return node.hasChildren();
}


public void setAlien(boolean alien) {
	this.isalien = alien;
}

}


