package edu.rice.cs.hpctree;

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


	public void setCurrentLevel(int currentLevel) {
		this.currentLevel = currentLevel;
	}
}
