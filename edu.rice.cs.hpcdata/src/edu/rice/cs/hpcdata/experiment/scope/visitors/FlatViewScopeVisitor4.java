package edu.rice.cs.hpcdata.experiment.scope.visitors;

import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.multimap.list.FastListMultimap;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.scope.AlienScope;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.EntryScope;
import edu.rice.cs.hpcdata.experiment.scope.FileScope;
import edu.rice.cs.hpcdata.experiment.scope.GroupScope;
import edu.rice.cs.hpcdata.experiment.scope.InstructionScope;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.LoadModuleScope;
import edu.rice.cs.hpcdata.experiment.scope.LoopScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.ScopeVisitType;
import edu.rice.cs.hpcdata.experiment.scope.StatementRangeScope;
import edu.rice.cs.hpcdata.experiment.scope.filters.ExclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpcdata.experiment.scope.filters.InclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpcdata.experiment.source.SourceFile;

/*************************************************************************
 * 
 * Special flat view visitor for meta.db database (db version 4)
 *
 *************************************************************************/
public class FlatViewScopeVisitor4 extends FlaViewScopeBuilder implements IScopeVisitor 
{
	private final IntObjectHashMap<FlatScopeInfo> mapIdToFlatScopeInfo;
	private final IntObjectHashMap<Scope> mapIdToScope;
	private final FastListMultimap<Integer, Scope> mapIdToCombinedScopes;
	
	private final RootScope root;
	private final InclusiveOnlyMetricPropagationFilter inclusiveFilter;
	private final ExclusiveOnlyMetricPropagationFilter exclusiveFilter;

	
	public FlatViewScopeVisitor4(RootScope rootFlatTree) {
		mapIdToFlatScopeInfo  = new IntObjectHashMap<>();
		mapIdToScope          = new IntObjectHashMap<>();
		mapIdToCombinedScopes = FastListMultimap.newMultimap();

		this.root = rootFlatTree;

		inclusiveFilter = new InclusiveOnlyMetricPropagationFilter((Experiment) rootFlatTree.getExperiment());
		exclusiveFilter = new ExclusiveOnlyMetricPropagationFilter((Experiment) rootFlatTree.getExperiment());
	}

	public void visit(Scope scope, ScopeVisitType vt) 				{ 
		if (scope instanceof InstructionScope) {
			add(scope, vt, true, false);
		}
	}
	public void visit(RootScope scope, ScopeVisitType vt) 			{ /* unused */ }
	public void visit(LoadModuleScope scope, ScopeVisitType vt) 	{ /* unused */ }
	public void visit(FileScope scope, ScopeVisitType vt) 			{ /* unused */ }
	public void visit(AlienScope scope, ScopeVisitType vt) 			{ /* unused */ }
	public void visit(StatementRangeScope scope, ScopeVisitType vt) { /* unused */ }
	public void visit(GroupScope scope, ScopeVisitType vt) 			{ /* unused */ }

	public void visit(CallSiteScope scope, ScopeVisitType vt) 		{ 
		add(scope,vt, true, false); 
	}
	public void visit(LineScope scope, ScopeVisitType vt) 			{ 
		add(scope,vt, true, false); 
	}
	public void visit(LoopScope scope, ScopeVisitType vt) 			{
		add(scope,vt, true, false); 
	}
	public void visit(ProcedureScope scope, ScopeVisitType vt) 		{		
		if (scope.isTopDownProcedure())
			return;
		add(scope, vt, true, false); 
	}
	
	private void add(Scope scope, ScopeVisitType vt, boolean inclusive, boolean exclusive) {
		add(scope, vt, scope, inclusive, exclusive);
	}
	
	private void add(Scope scope, ScopeVisitType vt, Scope metricScope, boolean inclusive, boolean exclusive) {
		int id = scope.getCCTIndex();
		
		if (vt == ScopeVisitType.PreVisit) {
			var flatScope  = getFlatCounterPart(scope);
			
			combine(id, flatScope.flatLM, metricScope, exclusive);
			combine(id, flatScope.flatFile, metricScope, exclusive);
			combine(id, flatScope.flatScope, metricScope, true);

			if (scope instanceof CallSiteScope || scope instanceof InstructionScope) {
				ProcedureScope procScope;
				if (scope instanceof CallSiteScope) {
					procScope = ((CallSiteScope) scope).getProcedureScope();
				} else {
					procScope = ((InstructionScope) scope).getProcedureScope();
				}
				var procFlatScope = getFlatCounterPart(procScope);

				combine(id, procFlatScope.flatLM, metricScope, true);
				combine(id, procFlatScope.flatFile, metricScope, true);
				combine(id, procFlatScope.flatScope, metricScope, true);
			}

		} else {
			var listScopes = mapIdToCombinedScopes.get(id);
			if (listScopes == null || listScopes.isEmpty())
				return;
			var iterator = listScopes.iterator();
			while(iterator.hasNext()) {
				var scopeCombined = iterator.next();
				scopeCombined.decrementCounter();
				assert(scopeCombined.getCounter() >= 0);
			}
		}
	}
	
	
	private FlatScopeInfo getFlatCounterPart(Scope scope) {

		var flatScope = mapIdToFlatScopeInfo.get(scope.getFlatIndex());
		if (flatScope == null) {
			flatScope = createFlatScope(scope);
			
			// in case a procedure scope, we link the scope to the file
			// for other scopes, we need to find the parent to link to
			if (!(scope instanceof ProcedureScope)) {
				// find the flat counter-part of the parent.
				// create it if it doesn't exist
				var flatParent = getTheParentFlatScope(scope);
				if (flatParent != null) {
					assert(flatScope.flatScope.getParentScope() == null);					
					addChild(flatParent.flatScope, flatScope.flatScope);
				}
			}
			
			mapIdToFlatScopeInfo.put(scope.getFlatIndex(), flatScope);
		}
		return flatScope;
	}
	
	
	private FlatScopeInfo getTheParentFlatScope(Scope cctScope) {
		Scope parent = cctScope.getParentScope();
		
		if (parent instanceof RootScope || parent instanceof EntryScope)
			return null;
		
		if (parent instanceof CallSiteScope) {
			parent = ((CallSiteScope) parent).getProcedureScope();
		}
		var flatScope = mapIdToFlatScopeInfo.get(parent.getFlatIndex());
		if (flatScope != null)
			return flatScope;
		return createFlatScope(parent);		
	}
	
	
	private void combine(int id, Scope target, Scope source, boolean exclusive) {
		// add inclusive cost only the scope is the outermost in the tree
		// if we have recursive node such as:
		//    a -> a -> a -> ...
		// then only the inclusive cost of the first `a` is considered.
		assert(target.getCounter() >= 0);
		
		if (target.getCounter() == 0) {
			target.combine(source, inclusiveFilter);
		}
		// add exclusive cost
		if (exclusive) {
			target.combine(source, exclusiveFilter);
		}
		target.incrementCounter();
		mapIdToCombinedScopes.put(id, target);
	}
	
	private FlatScopeInfo createFlatScope(Scope cctScope) {
		// create the flat load module
		LoadModuleScope lms;
		if (cctScope instanceof InstructionScope) {
			lms = ((InstructionScope) cctScope).getLoadModule();
		} else if (cctScope instanceof ProcedureScope) {
			lms = ((ProcedureScope) cctScope).getLoadModule();
		} else {
			var proc = findEnclosingProcedure(cctScope);
			lms = proc.getLoadModule();
		}
		var flatLm = mapIdToScope.get(lms.getFlatIndex());
		if (flatLm == null) {
			flatLm = lms.duplicate();	
			flatLm.setRootScope(root);
			addChild(root, flatLm);
			
			mapIdToScope.put(lms.getFlatIndex(), flatLm);
		}
		
		// create the flat file scope
		SourceFile sf  = cctScope.getSourceFile();
		int fileID = lms.getFlatIndex() << 13 | sf.getFileID();
		Scope flatFile = mapIdToScope.get(fileID);
		if (flatFile == null) {
			flatFile = new FileScope(root, sf, fileID);
			flatFile.setRootScope(root);
			addChild(flatLm, flatFile);
			
			mapIdToScope.put(fileID, flatFile);
		}
		
		// create the flat scope
		FlatScopeInfo flatInfo = new FlatScopeInfo();
		flatInfo.flatScope = cctScope.duplicate();
		flatInfo.flatScope.setRootScope(root);
		
		flatInfo.flatFile = (FileScope) flatFile;
		flatInfo.flatLM   = (LoadModuleScope) flatLm;
		
		if (flatInfo.flatScope instanceof ProcedureScope)
			addChild(flatInfo.flatFile, flatInfo.flatScope);
		
		return flatInfo;
	}
}
