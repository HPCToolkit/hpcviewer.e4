package edu.rice.cs.hpc.data.experiment.scope.visitors;

import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class DuplicateScopeTreesVisitor extends BaseDuplicateScopeTreesVisitor {

	/****
	 * Class for duplicating a tree, including its metric values 
	 * 
	 * @param newRoot
	 */
	public DuplicateScopeTreesVisitor(Scope newRoot) {
		super(newRoot, 0);
	}

	
	public DuplicateScopeTreesVisitor(Scope newRoot, int offset) {
		super( newRoot, offset );
	}
	
	//@Override
	protected Scope findMatch(Scope parent, Scope toMatch) {
		// for duplication, everything matches
		return null;
	}

}
