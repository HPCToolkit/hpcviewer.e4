package edu.rice.cs.hpcdata.experiment.scope.visitors;

import java.util.*;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.AlienScope;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.FileScope;
import edu.rice.cs.hpcdata.experiment.scope.GroupScope;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.LoadModuleScope;
import edu.rice.cs.hpcdata.experiment.scope.LoopScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.ScopeVisitType;
import edu.rice.cs.hpcdata.experiment.scope.StatementRangeScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope.ProcedureType;
import edu.rice.cs.hpcdata.experiment.scope.filters.ExclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpcdata.experiment.scope.filters.InclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpcdata.experiment.source.SourceFile;
import edu.rice.cs.hpcdata.util.Constants;


/*************************************************************************************************
 * Class to create Flat tree based on calling context tree
 * 
 * REMARK: THIS CODE IS NOT COMPATIBLE WITH OLD DATABASE !!!
 *  
 *
 *************************************************************************************************/

public class FlatViewScopeVisitor implements IScopeVisitor 
{
	private static final String SEPARATOR_ID = ":";
	
	private final Experiment exp;
	
	private Hashtable<Integer, LoadModuleScope> htFlatLoadModuleScope;
	private Hashtable<String, FileScope> htFlatFileScope;
	private HashMap<String, FlatScopeInfo> htFlatScope;
	private HashMap<String, List<Scope>> htFlatCostAdded;
	
	private RootScope root_ft;
	
	private InclusiveOnlyMetricPropagationFilter inclusive_filter;
	private ExclusiveOnlyMetricPropagationFilter exclusive_filter;
	
	//final private boolean debug = false;
	
	/******************************************************************
	 * Constructor
	 * @param exp: experiment
	 * @param root: the root of the tree
	 ******************************************************************/
	public FlatViewScopeVisitor( Experiment exp, RootScope root) {
		this.htFlatLoadModuleScope = new Hashtable<Integer, LoadModuleScope>();
		this.htFlatFileScope = new Hashtable<String, FileScope>();
		this.htFlatScope     = new HashMap<String, FlatScopeInfo>();
		this.htFlatCostAdded = new HashMap<String, List<Scope>>();
		
		this.root_ft = root;
		this.exp = exp;
		
		this.inclusive_filter = new InclusiveOnlyMetricPropagationFilter( exp );
		this.exclusive_filter = new ExclusiveOnlyMetricPropagationFilter( exp );
	}
	
	
	public void visit(Scope scope, ScopeVisitType vt) 				{ }
	public void visit(RootScope scope, ScopeVisitType vt) 			{ }
	public void visit(LoadModuleScope scope, ScopeVisitType vt) 	{ }
	public void visit(FileScope scope, ScopeVisitType vt) 			{ }
	public void visit(AlienScope scope, ScopeVisitType vt) 			{ }
	public void visit(StatementRangeScope scope, ScopeVisitType vt) { }
	public void visit(GroupScope scope, ScopeVisitType vt) 			{ }

	public void visit(CallSiteScope scope, ScopeVisitType vt) 		{ 
		// by default we don't add exclusive cost of the call site
		//	to the file and load module scope.
		// This is because we will add the exclusive cost of the line scope
		//  and we don't want to compute the double cost.
		//
		// However, for meta.db this may not be the case since a call site
		// 	can be a leaf, and it may have no children.
		//  in this case, we can add its exclusive cost to the file or lm.
		// 
		// Corner case: a call site that has another call site, its exclusive
		//	cost can be added to the file and load module scope.
		// This is allowed only if it has no line scope
		
		boolean add_exclusive = !scope.hasChildren();
		if (scope.hasChildren()) {
			add_exclusive = !scope.getChildren().
								  stream().
								  anyMatch(child -> !(child instanceof CallSiteScope));
		}
		
		add(scope,vt, true, add_exclusive); 
	}
	public void visit(LineScope scope, ScopeVisitType vt) 			{ 
		add(scope,vt, true, true); 
	}
	public void visit(LoopScope scope, ScopeVisitType vt) 			{
		add(scope,vt, true, !scope.hasChildren()); 
	}
	public void visit(ProcedureScope scope, ScopeVisitType vt) 		{		
		if (scope.isTopDownProcedure())
			return;
		add(scope, vt, true, false); 
	}

	
	/******************************************************************
	 * Create or add a flat scope based on the scope from CCT
	 * @param scope
	 * @param vt
	 * @param add_inclusive: flag if an inclusive cost had to be combined in flat/module scope
	 * @param add_exclusive: flag if an exclusive cost had to be combined in flat/module scope
	 ******************************************************************/
	private void add( Scope scope, ScopeVisitType vt, boolean add_inclusive, boolean add_exclusive ) {
		
		String id = this.getID(scope); 

		if (vt == ScopeVisitType.PreVisit ) {
			//--------------------------------------------------------------------------
			// Pre-visit
			//--------------------------------------------------------------------------
			List<Scope> flat_info = htFlatCostAdded.get( id );
			if (flat_info != null) {
				htFlatCostAdded.remove(id);
			}

			FlatScopeInfo objFlat = getFlatCounterPart(scope, scope, id);
			
			//--------------------------------------------------------------------------
			// Aggregating metrics to load module and flat scope
			// Notes: this is not correct for Derived incremental metrics
			//--------------------------------------------------------------------------
			if (objFlat != null) {
				addCostIfNecessary(id, objFlat.flatLM, scope, add_inclusive, add_exclusive);
				addCostIfNecessary(id, objFlat.flatFile, scope, add_inclusive, add_exclusive);

				//--------------------------------------------------------------------------
				// For call site, we need also to create its procedure scope
				//--------------------------------------------------------------------------
				if (scope instanceof CallSiteScope) {
					ProcedureScope proc_cct_s = ((CallSiteScope) scope).getProcedureScope();
					getFlatCounterPart(proc_cct_s, scope, id);
				}
			}
		} else {
			
			//--------------------------------------------------------------------------
			// Post visit
			//--------------------------------------------------------------------------
			List<Scope> flat_info = htFlatCostAdded.get( id );
			if (flat_info != null)
				for (Scope node: flat_info) {
					node.decrementCounter();
				}
		}
	}
	
	
	/****************************************************************************
	 * Get the flat counterpart of the scope cct:
	 * - check if the flat counter part already exist
	 * -- if not, create a new one
	 * - get the flat file counter part exists
	 * 
	 * @param scopeCCT
	 * @return
	 ****************************************************************************/
	private FlatScopeInfo getFlatScope( Scope cct_s ) {
		//-----------------------------------------------------------------------------
		// get the flat scope
		//-----------------------------------------------------------------------------
		String id = getID(cct_s);
		FlatScopeInfo flat_info_s = this.htFlatScope.get( id );
		
		if (flat_info_s == null) {

			//-----------------------------------------------------------------------------
			// Initialize the flat scope
			//-----------------------------------------------------------------------------
			flat_info_s = new FlatScopeInfo();
			
			//-----------------------------------------------------------------------------
			// finding enclosing procedure of this cct scope:
			// if it is a call site, then the file and the module can be found in the scope
			// for others, we need to find the enclosing procedure iteratively
			//-----------------------------------------------------------------------------
			ProcedureScope proc_cct_s;
			if (cct_s instanceof CallSiteScope) {
				proc_cct_s = ((CallSiteScope)cct_s).getProcedureScope();
			} else {
				proc_cct_s = findEnclosingProcedure(cct_s);
			}

			// ideally we shouldn't allow place folders (fake procedures) to be created in the flat tree.
			// However, some place folders like <gpu copyin> and <gpu copyout> contains metric values 
			// hence should be added in the tree.
			// That's why we shouldn't throw fake procedures here.
			
			if (proc_cct_s == null || proc_cct_s.isTopDownProcedure()) {
				return null;
			}
			
			//-----------------------------------------------------------------------------
			// Initialize the flat scope of this cct
			//-----------------------------------------------------------------------------
			flat_info_s.flatScope = cct_s.duplicate();
			flat_info_s.flatScope.setRootScope(root_ft);
			
			//-----------------------------------------------------------------------------
			// save the info into hashtable
			//-----------------------------------------------------------------------------
			this.htFlatScope.put( id, flat_info_s);

			//-----------------------------------------------------------------------------
			// for inline macro, we don't need to attach the file and load module
			// an inline macro node will be attached directly to its parent
			//-----------------------------------------------------------------------------
			if (isInlineMacro(flat_info_s.flatScope)) {
				return flat_info_s;
			}
			
			//-----------------------------------------------------------------------------
			// Initialize the load module scope
			//-----------------------------------------------------------------------------
			flat_info_s.flatLM = this.createFlatModuleScope(proc_cct_s);

			//-----------------------------------------------------------------------------
			// Initialize the flat file scope
			//-----------------------------------------------------------------------------
			flat_info_s.flatFile = this.createFlatFileScope(proc_cct_s, flat_info_s.flatLM);
			
			//-----------------------------------------------------------------------------
			// Attach the scope to the file if it is a procedure
			//-----------------------------------------------------------------------------
			if (flat_info_s.flatScope instanceof ProcedureScope) {
				this.addToTree(flat_info_s.flatFile, flat_info_s.flatScope);
			}
		}
		
		return flat_info_s;
	}
	
	/*****************************************************************
	 * Create the flat view of a load module
	 * @param proc_cct_s
	 * @return
	 *****************************************************************/
	private LoadModuleScope createFlatModuleScope(ProcedureScope proc_cct_s) {
		LoadModuleScope lm = proc_cct_s.getLoadModule();
		LoadModuleScope lm_flat_s = null;
		
		// some old database do not provide load module information
		if (lm != null)  {
			lm_flat_s = this.htFlatLoadModuleScope.get(lm.getFlatIndex());
			if (lm_flat_s == null) {
				// no load module has been created. we allocate a new one
				lm_flat_s = (LoadModuleScope) lm.duplicate();
				lm_flat_s.setRootScope(root_ft);
				// attach the load module to the root scope
				this.addToTree(root_ft, lm_flat_s);
				// store this module into our dictionary
				this.htFlatLoadModuleScope.put(lm.getFlatIndex(), lm_flat_s);
			}
		}
		return lm_flat_s;
	}
	
	/*****************************************************************
	 * generate a unique file ID which depends on the load module:
	 * unique file ID = load_module_ID + file_ID 
	 * 
	 * @param file
	 * @param lm
	 * 
	 * @return
	 *****************************************************************/
	private String getUniqueFileID(SourceFile file, LoadModuleScope lm)
	{
		final String separator = String.valueOf(SEPARATOR_ID);
		
		int lmId   = lm != null   ? lm.getFlatIndex() : 0;
		int fileId = file != null ? file.getFileID()  : 0;
		
		return lmId + separator + fileId;
	}
	
	
	/*****************************************************************
	 * Create the flat view of a file scope
	 * @param cct_s
	 * @param flat_lm
	 * @return
	 *****************************************************************/
	private FileScope createFlatFileScope(Scope cct_s, LoadModuleScope flat_lm) {
		SourceFile src_file = cct_s.getSourceFile();	
		String unique_file_id = getUniqueFileID(src_file, flat_lm);
		FileScope flat_file = this.htFlatFileScope.get( unique_file_id );
		
		//-----------------------------------------------------------------------------
		// ATTENTION: it is possible that a file can be included into more than one load module
		//-----------------------------------------------------------------------------
		if ( (flat_file == null) ){
			return getNewFileScope(src_file, flat_lm, unique_file_id);			
		}
		
		Scope parent_lm = flat_file.getParentScope();

		// check if the load module the existing file is the same with the scope's load module
		if (parent_lm.getFlatIndex() != flat_lm.getFlatIndex() ) {
			// the same file in different load module scope !!!
			flat_file = getNewFileScope(src_file, flat_lm, unique_file_id);
		}

		return flat_file;
	}
	
	
	/*****************************************************************
	 * Create a new file scope (this procedure will NOT check if the file already exists or not) !
	 * @param src_file
	 * 			source file object
	 * @param lm_s
	 * 			the parent load module
	 * 
	 * @return a new file scope 
	 *****************************************************************/
	private FileScope getNewFileScope(SourceFile src_file, LoadModuleScope lm_s, String unique_file_id) {

		FileScope file_s =  new FileScope( this.root_ft, src_file, unique_file_id.hashCode() );
		//------------------------------------------------------------------------------
		// if load module is undefined, then we attach the file scope to the root scope
		//------------------------------------------------------------------------------
		if (lm_s == null)
			this.addToTree(root_ft, file_s);
		else
			this.addToTree(lm_s, file_s);
		this.htFlatFileScope.put( unique_file_id, file_s);

		return file_s;
	}
	
	
	/*****************************************************************
	 * construct the flat view of a cct scope
	 * @param cct_s the original cct scope
	 * @param cct_s_metrics cct that metrics will be applied to the flat scope
	 * @param id original id
	 * 
	 * @return FlatScopeInfo if the flat scope can be created
	 *****************************************************************/
	private FlatScopeInfo getFlatCounterPart( Scope cct_s, Scope cct_s_metrics, String id) {
		if (cct_s == null)
			return null;
		
		// -----------------------------------------------------------------------------
		// Get the flat scope of the parent 	
		// -----------------------------------------------------------------------------
		Scope cct_parent_s = cct_s.getParentScope() ;
		Scope flat_enc_s = null;

		if (cct_parent_s != null) {
			if (cct_parent_s instanceof RootScope) {
				// ----------------------------------------------
				// main procedure: no parent
				// ----------------------------------------------
				flat_enc_s = null;
			} else {
				FlatScopeInfo flat_enc_info = null;
				if ( cct_parent_s instanceof CallSiteScope ) {
					// ----------------------------------------------
					// parent is a call site: find the procedure scope of this call
					// ----------------------------------------------
					ProcedureScope proc_cct_s = ((CallSiteScope)cct_parent_s).getProcedureScope(); 
					flat_enc_info = getFlatScope(proc_cct_s);

				} else {					
					flat_enc_info = getFlatScope(cct_parent_s);
				}
				if (flat_enc_info != null)
					flat_enc_s = flat_enc_info.flatScope;
			}
		}

		FlatScopeInfo objFlat = getFlatScope(cct_s);
		if (objFlat == null)
			return null;

		if (flat_enc_s != null) {
			// Check if there is a cyclic dependency between the child and the ancestors
			if (!isCyclicDependency(flat_enc_s, objFlat.flatScope)) {
				// no cyclic dependency: it's safe to add to the parent
				this.addToTree(flat_enc_s, objFlat.flatScope);				
			} else {
				// A very rare case: cyclic dependency
				// TODO: we should create a new copy and attach it to the tree
				// but this will cause an issue for adding metrics and decrement counter
				// at the moment we just avoid cyclic dependency				
			}
		}
		this.addCostIfNecessary(id, objFlat.flatScope, cct_s_metrics, true, true);
		
		return objFlat;
		
	}
	
	
	/***********************************************************
	 * Retrieve the ID given a scope
	 * <p>
	 * A flat ID is the name of the class class concatenated by the scope's struct ID
	 * 	(This is to force to have different ID for different classes
	 *   since hpcprof may generate the same ID for different type of scopes)
	 *   </p>
	 * for a call site, we need to add the flat ID of the called procedure
	 *  (this is to ensure a flat's call site has different ID.
	 *   however, it doesn't solve if the called procedures have the same ID)
	 * @param scope
	 * @return
	 ***********************************************************/
	private String getID( Scope scope ) {
		if (scope == null)
			return SEPARATOR_ID;
		
		var hash_id = new StringBuilder();

		// --------------------------------------------------------
		// Why do we need to reconstruct the flat id?
		// Because some versions of databases screw up the flat id, and
		//  some of them remove redundancies, some allow duplicates
		//
		// Steps of the Id reconstruction works as follows:
		// (1) <class, flat_id> 
		//     for sparse database, the first step is sufficient since
		//     the metadb parser (DataMeta class) will assign the flat id
		//     properly for flat view.
		// (2) <class, flat_id, load_module, file_source, line_num>
		// (3) <class, flat_id, load_module, file_source, procedure, line_num>
		//
		// if the node is nested (alien = true): include the parent id
		// (4a) <class, flat_id, load_module, file_source, procedure, line_num, parent_id>
		//
		// or, if the parent is inlined (alien = true), include the parent as well
		// (4b) <class, flat_id, load_module, file_source, procedure, line_num, parent_id>
		// --------------------------------------------------------

		// ------
		// step (1) <class, flat_id>
		// ------
		final String class_type = scope.getClass().getSimpleName();
		if (class_type != null) {
			hash_id.insert(0, class_type.substring(0, 2));
		}
		hash_id.append(scope.getFlatIndex());

		// -----
		// special case: database version 4 (sparse database)
		// since the parser (DataMeta class) assigns properly the flat id for us,
		// there is no need to reconstruct a new-id here.
		// -----
		if (exp.getMajorVersion() == Constants.EXPERIMENT_SPARSE_VERSION)
			return hash_id.toString();

		// ------
		// step (2) <class, flat_id, load_module, file_source, line_num>
		// ------
		var source_file = scope.getSourceFile();
		hash_id.append(SEPARATOR_ID);
		hash_id.append(source_file.getFileID());
		
		var proc_scope = findEnclosingProcedure(scope);
		if (proc_scope == null)
			return hash_id.toString();
		
		hash_id.append(SEPARATOR_ID);
		hash_id.append(proc_scope.getLoadModule().getFlatIndex());

		// ------
		// (3) <class, flat_id, load_module, file_source, procedure, line_num>
		// ------
		hash_id.append(SEPARATOR_ID);
		hash_id.append(proc_scope.getFlatIndex());

		hash_id.append(SEPARATOR_ID);
		hash_id.append(scope.getFirstLineNumber());

		// ------
		// (4a) <class, flat_id, load_module, file_source, procedure, line_num, parent_id>
		// ------

		// ------
		// (4b) <class, flat_id, load_module, file_source, procedure, line_num, parent_id>
		// ------

		return hash_id.toString();
	}
	
	
	/***********************************************************
	 * Add a child as the subscope of a parent
	 * @param parent
	 * @param child
	 ***********************************************************/
	private void addToTree( Scope parent, Scope child ) {
		int nkids = parent.getSubscopeCount();
		
		//-------------------------------------------------------------------------------
		// search for the existing kids. If the kid is already added, then we don't need
		// 	to add it again
		//-------------------------------------------------------------------------------
		for (int i=0; i<nkids; i++) {
			Scope kid = parent.getSubscope(i);
			if ( this.isTheSameScope(kid, child) )
				return;
		}
		assert(child.getParentScope() == null);
		addChild(parent, child);
	}
	
	/*****************************************************************
	 * check if the child is also the ancestor of the parent (cyclic dependency)
	 * @param parent
	 * @param child
	 * @return boolean true if the child is the ancestor of the parent 
	 ****************************************************************/
	private boolean isCyclicDependency(Scope parent, Scope child)
	{
		Scope ancestor = parent;
		while (ancestor != null && !(ancestor instanceof RootScope))
		{
			if (ancestor == child) {
				// cyclic
				return true;
			}
			ancestor = ancestor.getParentScope();
		}
		return false;
	}
	
	/*****************************************************************
	 * check if two scopes are equal (same cct index)
	 * @param s1
	 * @param s2
	 * @return true if they are the same
	 ****************************************************************/
	private boolean isTheSameScope(Scope s1, Scope s2) {
				
		// are s1 and s2 the same class ?
		if ( s1.getClass() != s2.getClass() )
			return false;
		return (s1.getCCTIndex() == s2.getCCTIndex());
	}
	

	/******************************************************************
	 * check if the scope is an inlined macro
	 * @param scope
	 * @return true if it is an inlined macro
	 *****************************************************************/
	private boolean isInlineMacro(Scope scope) {
		if (!(scope instanceof ProcedureScope))
			return false;
		
		ProcedureScope ps = (ProcedureScope) scope;
		return ps.getProcedureType() == ProcedureType.ProcedureInlineMacro;
	}
	
	/******************************************************************
	 * add child to the parent
	 * 
	 * @param parent
	 * @param child
	 ****************************************************************/
	private void addChild(Scope parent, Scope child) {
		parent.addSubscope(child);
		child.setParentScope(parent);
	}
	
	
	/***********************************************************
	 * Iteratively finding an enclosing procedure of a CCT scope
	 * @param cct_s
	 * @return
	 ***********************************************************/
	private ProcedureScope findEnclosingProcedure(Scope cct_s)
	{
		if (cct_s == null)
			return null;
			
		if (cct_s instanceof ProcedureScope) 
			return (ProcedureScope) cct_s;
		
		Scope parent = cct_s.getParentScope();
		while(parent != null) {
			if (parent instanceof CallSiteScope) {
				return ((CallSiteScope) parent).getProcedureScope();
			}
			if (parent instanceof ProcedureScope) {
				ProcedureScope proc = (ProcedureScope) parent;
				if (!proc.isAlien())
					return proc;
			}
			if (parent instanceof RootScope) return null;
			parent = parent.getParentScope();
		}
		return null;
	}

	
	/***********************************************************
	 * check if a scope has been assigned as the outermost instance
	 * @param scope
	 * @return
	 ***********************************************************/
	private boolean isOutermostInstance(Scope scope) {
		return scope.getCounter() == 1;
	}

	
	/***********************************************************
	 * add the cost of the cct into the flat scope if "necessary"
	 * Necessary means: add the inclusive cost if the cct scope if the outermost scope
	 * @param scopeFlat
	 * @param scopeCCT
	 ***********************************************************/
	private void addCostIfNecessary( String objCode, Scope scopeFlat, Scope scopeCCT, boolean addInclusive, boolean addExclusive ) {
		if (scopeFlat == null)
			return;
		
		scopeFlat.incrementCounter();
			
		if (addInclusive && isOutermostInstance(scopeFlat)) {
			scopeFlat.combine(scopeCCT, inclusive_filter);
		}
		if (addExclusive) {
			if (scopeFlat instanceof CallSiteScope && scopeCCT instanceof CallSiteScope) {
				Scope scopeSource = scopeCCT;
				if (this.exp.getMajorVersion() == Constants.EXPERIMENT_DENSED_VERSION) {
					// fix issue #229: only in old database we use the line scope to combine the metric.
					// for the new database, we assume the cost of the line scope of the call is zero,
					// hence we use the cost of the original call site scope
					CallSiteScope csScope = (CallSiteScope) scopeCCT;
					scopeSource = csScope.getLineScope();
				}
				scopeFlat.combine(scopeSource, exclusive_filter);
			} else {
				scopeFlat.combine(scopeCCT, exclusive_filter);
			}
		}
		//-----------------------------------------------------------------------
		// store the flat scopes that have been updated  
		//-----------------------------------------------------------------------

		List<Scope> listAddedScopes = htFlatCostAdded.get( objCode );
		if (listAddedScopes == null) {
			listAddedScopes = new ArrayList<>();
		}
		listAddedScopes.add(scopeFlat);

		htFlatCostAdded.put(objCode, listAddedScopes);
	}

	
	/*************************************************************************
	 * Each scope in the flat view has to be linked with 3 enclosing scopes:
	 * <ul>
	 *  <li> load module
	 *  <li> file
	 *  <li> procedure
	 * </ul>
	 *************************************************************************/
	private class FlatScopeInfo {
		LoadModuleScope flatLM;
		FileScope flatFile;
		Scope flatScope;
	}
}
