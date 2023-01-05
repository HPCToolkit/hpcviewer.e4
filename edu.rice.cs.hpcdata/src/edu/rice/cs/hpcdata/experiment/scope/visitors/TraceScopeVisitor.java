package edu.rice.cs.hpcdata.experiment.scope.visitors;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
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
import edu.rice.cs.hpcdata.util.CallPath;
import edu.rice.cs.hpcdata.util.ICallPath;


/*****************************************************
 * <p>
 * Visitor class to gather call stack and its depth.
 * This class is designed to be used for the newer database
 * (version 4 or newer). It collects the current context id,
 * the enclosing procedure and the depth.
 * </p>
 * <p>
 * The older databases only consider procedures and assume
 * the trace id is always at the leaf. This is not valid anymore
 * starting with the version 4. We collect all the ids, and
 * associate it with the enclosed procedure. 
 * </p>
 * If the top-down tree has the following structure:
 * <pre>
 *   func A
 *     loop L
 *        Line S
 *          Call func B
 * </pre>
 * If we assumne func A is in depth 4, then loop L and line S 
 * have the same depth (i.e. depth 4). Func B will have depth 5.
 * <p>
 * In trace view's call stack it will display only the call stack
 * of procedure frames although the trace id can be at the loop 
 * or line level.
 * </p>
 * tldr; hate backward compatibility.
 *****************************************************/
public class TraceScopeVisitor implements IScopeVisitor 
{
	private final ICallPath callpath;
	private int currentDepth;
	private int maxDepth;
	
	public TraceScopeVisitor() {
		callpath = new CallPath();
		currentDepth = 0;
		// this is to fix the problem with "division by zero"
		// we force the max depth to be 1 since it will be used elsewhere		
		maxDepth = 1;
	}

	/***
	 * Retrieve the ICallPath object created
	 * 
	 * @return {@code ICallPath}
	 */
	public ICallPath getCallPath() {
		return callpath;
	}
	
	/***
	 * Return the maximum depth in this tree.
	 * The depth only counts the number of procedure frames,
	 * not the loops or the lines.
	 * 
	 * @return {@code int}
	 * 			the maximum depth
	 */
	public int getMaxDepth() {
		return maxDepth;
	}
	
	@Override
	public void visit(LineScope scope, ScopeVisitType vt) { update(scope, vt); }

	@Override
	public void visit(StatementRangeScope scope, ScopeVisitType vt) {/* unused */}

	@Override
	public void visit(LoopScope scope, ScopeVisitType vt) { update(scope, vt); }

	@Override
	public void visit(CallSiteScope scope, ScopeVisitType vt) { 
		// fix issue #233: no separate treatment between line scope and call site
		update(scope, vt);
	}

	@Override
	public void visit(ProcedureScope scope, ScopeVisitType vt) { update(scope, vt); }

	@Override
	public void visit(FileScope scope, ScopeVisitType vt) {/* not treated in this class */}

	@Override
	public void visit(GroupScope scope, ScopeVisitType vt) {/* unused */}

	@Override
	public void visit(LoadModuleScope scope, ScopeVisitType vt) {/* not treated in this class */}

	@Override
	public void visit(RootScope scope, ScopeVisitType vt) {/* not treated in this class */}

	@Override
	public void visit(Scope scope, ScopeVisitType vt) {
		// issue #233: needs to include the instruction scope as part of the traces
		if (scope instanceof InstructionScope) {
			update(scope, vt);
		}
	}

	
	private void update(Scope scope, ScopeVisitType vt) {
		if (scope == null)
			return;
		
		if (vt == ScopeVisitType.PreVisit) {
			Scope current = scope;
			
			if (! CallPath.isTraceScope(current)) {
	 			while ( (current != null) && 
	 					!CallPath.isTraceScope(current) &&
						!(current instanceof RootScope)) {
					current = current.getParentScope();
				}
			}
			if (CallPath.isTraceScope(scope)) {
				currentDepth++;
				maxDepth = Math.max(maxDepth, currentDepth);
			}
			if (current == null)
				// this may never happen
				// however, just in case if the database corrupts, we force
				// to make it the same as the scope.
				current = scope;
			
			callpath.addCallPath(scope.getCpid(), current, currentDepth);
			
			// need to add the line scope (which is the parent for prof2 database)
			// because this parent can be removed later on during Calling Context Reassignment
			if (scope instanceof CallSiteScope) {
				CallSiteScope cs = (CallSiteScope) scope;
				callpath.addCallPath(cs.getLineScope().getCCTIndex(), current, currentDepth);
			}

		} else {
			if (CallPath.isTraceScope(scope))
				currentDepth--;
		}
	}
}
