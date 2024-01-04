package edu.rice.cs.hpctree;

import java.util.Comparator;
import java.util.List;

import edu.rice.cs.hpcbase.IDatabase;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.IMergedScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import java.util.Collections;


/*******************************************************
 * 
 * Special class for bottom up tree where the sub-tree
 * is created dynamically
 *
 *******************************************************/
public class BottomUpScopeTreeData extends ScopeTreeData 
{

	public BottomUpScopeTreeData(IDatabase database, RootScope root, IMetricManager metricManager) {
		super(database, root, metricManager);
	}


	@Override
	public List<Scope> getChildren(Scope scope) {
		if (scope == null)
			return Collections.emptyList();
		
		// special case for bottom-up tree:
		// we need to build the tree dynamically, which mean we don't know the children yet.
		// here, we build the subtree (children) on demand, and then sort them
		if (scope instanceof IMergedScope) {
			var listChildren = scope.getChildren();
			if (listChildren != null) {
				
				Comparator<Scope> comparator = getComparator(getSortedColumn(), getSortDirection()); 
				listChildren.sort(comparator);

				return listChildren;
			}
		}
		return super.getChildren(scope);
	}


	@Override
	public boolean hasChildren(Scope object) {
		// since the tree is created dynamically, we don't know if a node has children or not
		// Here, instead of using the default parent hasChildren(), we ask the scope to check
		// if it has children or not
		return object.hasChildren();
	}
}
