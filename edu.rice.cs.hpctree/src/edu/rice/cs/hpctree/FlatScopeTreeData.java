package edu.rice.cs.hpctree;

import java.util.List;

import org.eclipse.collections.impl.list.mutable.FastList;

import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

public class FlatScopeTreeData extends ScopeTreeData 
{
	private int currentLevel;

	public FlatScopeTreeData(RootScope root, IMetricManager metricManager) {
		super(root, metricManager);
		currentLevel = 0;
	}
	
	

	@Override
	public int getDepthOfData(Scope scope) {
		int depth  = super.getDepthOfData(scope);
		if (depth <= 1 || currentLevel == 0)
			return depth;
		
		// special treatment when the tree is flattened:
		// we need to adjust the depth to match the current level of flat
		// Imagine the original flat tree:
		// root0 (depth: 0)
		//  - a1 (depth: 1)
		//    - a2 (depth: 2)
		//  - b1
		//    - b2
		//
		// the flattened tree will be (a1 and b1 are removed):
		// root0 (depth: 0)
		//  - a2 (from depth 2 to depth: 1)
		//    - a3 (from depth 3 to depth: 2)
		//  - b2
		//    - b3
        int cdepth = depth - currentLevel;
        if (!scope.hasChildren() && cdepth <= 0) {
        	return 1;
        }
        return cdepth;		
	}

	
	@Override
	public List<Scope> getPath(Scope node) {
		FastList<Scope> path = FastList.newList();
		Scope current = node;
		
		// Only add the path up to level 1, do not include nodes in level 0.
		// This is important for a flattened tree where we remove the ancestors
		//
		// E.g the original tree as follows:
		// 		root -> module -> file -> procedure -> loop -> line
		//
		// if we flatten the module node, the tree becomes: 
		// 		root -> file -> procedure -> loop -> line
		//
		// Since the algorithm getPath is bottom-up approach, then we always
		// get the original path (the pointer to the parent doesn't change). 
		// To remedy this, we check if the node in the path
		// is always bigger than zero in the flat tree.
		
		while(current != null  &&  !isRootScope(current) && getDepthOfData(current)>0) {
			path.add(current);
			current = current.getParentScope();
		}
		return path.reverseThis();
	}


	public void setCurrentLevel(int currentLevel) {
		this.currentLevel = currentLevel;
	}
}
