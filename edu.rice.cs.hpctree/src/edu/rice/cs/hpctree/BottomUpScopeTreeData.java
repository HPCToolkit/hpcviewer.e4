package edu.rice.cs.hpctree;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.IMergedScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.TreeNode;
import edu.rice.cs.hpcdata.experiment.scope.filters.ExclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpcdata.experiment.scope.filters.InclusiveOnlyMetricPropagationFilter;

public class BottomUpScopeTreeData extends ScopeTreeData 
{
	private final ExclusiveOnlyMetricPropagationFilter exclusiveOnly;
	private final InclusiveOnlyMetricPropagationFilter inclusiveOnly;

	public BottomUpScopeTreeData(RootScope root, IMetricManager metricManager) {
		super(root, metricManager);

		exclusiveOnly = new ExclusiveOnlyMetricPropagationFilter((Experiment) metricManager);
    	inclusiveOnly = new InclusiveOnlyMetricPropagationFilter((Experiment) metricManager);
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<Scope> getChildren(Scope scope) {
		if (scope == null)
			return null;
		
		if (scope instanceof IMergedScope) {
			IMergedScope ms = (IMergedScope) scope;
			Object []children = ms.getAllChildren(inclusiveOnly, exclusiveOnly);
			if (children != null) {
				List<?> list = Arrays.asList(children);
				
				final BaseMetric metric = getSortedColumn() == 0 ? null : getMetric(getSortedColumn()-1);
				Comparator<TreeNode> comparator = new Comparator<TreeNode>() {

					@Override
					public int compare(TreeNode o1, TreeNode o2) {
						Scope s1 = (Scope) o1;
						Scope s2 = (Scope) o2;				
						return compareNodes(s1, s2, metric, getSortDirection());
					}
				};
				List<Scope> listChildren = (List<Scope>) list;
				listChildren.sort(comparator);

				return listChildren;
			}
		}
		return super.getChildren(scope);
	}


	@Override
	public boolean hasChildren(Scope object) {
		if (object instanceof IMergedScope) {
			return ((IMergedScope)object).hasScopeChildren();
		}
			
		return object.hasChildren();
	}
}
