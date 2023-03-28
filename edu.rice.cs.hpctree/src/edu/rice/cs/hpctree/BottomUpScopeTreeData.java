package edu.rice.cs.hpctree;

import java.util.Comparator;
import java.util.List;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.IMergedScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import java.util.Collections;

public class BottomUpScopeTreeData extends ScopeTreeData 
{

	public BottomUpScopeTreeData(RootScope root, IMetricManager metricManager) {
		super(root, metricManager);
	}


	@Override
	public List<Scope> getChildren(Scope scope) {
		if (scope == null)
			return Collections.emptyList();
		
		if (scope instanceof IMergedScope) {
			var listChildren = scope.getChildren();
			if (listChildren != null) {
				
				final BaseMetric metric = getSortedColumn() == 0 ? null : getMetric(getSortedColumn()-1);
				Comparator<Scope> comparator = (s1, s2) -> compareNodes(s1, s2, metric, getSortDirection()); 
				listChildren.sort(comparator);

				return listChildren;
			}
		}
		return super.getChildren(scope);
	}


	@Override
	public boolean hasChildren(Scope object) {
		return object.hasChildren();
	}
}
