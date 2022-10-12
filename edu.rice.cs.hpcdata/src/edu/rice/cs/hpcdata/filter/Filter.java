package edu.rice.cs.hpcdata.filter;

import java.util.ArrayList;
import java.util.List;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.visitors.FilterScopeVisitor;
import edu.rice.cs.hpcdata.experiment.scope.visitors.TraceScopeVisitor;


/************************************************************************
 * 
 * Filter CCT nodes class
 * <p>
 * This class consists only of one method: {@link filterExperiment} to
 * filter an experiment database given a set of filter rules.
 *
 ************************************************************************/
public class Filter 
{	
	private Filter() {
		// unused
	}
	
	/*************************************************************************
	 * filter an experiment database
	 * <p>caller needs to call post-process to ensure the callers tree and flat
	 * tree are also filtered </p>

	 * @param experiment
	 * 			The database
	 * @param filter
	 * 			The set of filter rules
	 * 
	 * @return int
	 * 			Number of nodes removed 
	 * @throws Exception 
	 *************************************************************************/
	public static int filterExperiment(Experiment experiment, IFilterData filter) throws Exception {
		int numFilteredNodes = 0;
		
		// filter the experiment if it is not null and it is in original form
		// (it isn't a merged database)
		if (experiment != null && !experiment.isMergedDatabase()) 
		{
			// ---------------------------------------
			// Before filtering: conserve the added user-derived metrics.
			// The reason is that when we reopen the database, we reset the
			// list of metrics and lose the user derived metrics.
			// ---------------------------------------
			List<BaseMetric> metrics = new ArrayList<>(experiment.getMetricCount());

			for (BaseMetric metric : experiment.getMetricList()) {
				if (metric instanceof DerivedMetric && 
					metric.getMetricType()==MetricType.UNKNOWN) {
					
					// only add user derived metrics, not all derived metrics
					//  provided by hpcprof
					
					metrics.add(metric);
				} else {
					metrics.add(metric.duplicate());
				}
			}
			var rawMetrics = experiment.getRawMetrics();

			// ---------------------------------------
			// filtering 
			// ---------------------------------------
			numFilteredNodes = filter(experiment, filter);
			
			// ---------------------------------------
			// put the original metrics and derived metrics back
			// ---------------------------------------
			experiment.setMetrics(metrics);
			experiment.setMetricRaw(rawMetrics);
			
			experiment.resetThreadData();

		}
		return numFilteredNodes;
	}


	/*************************************************************************
	 * Perform filtering the cct by:
	 * <ol>
	 * <li> reopening the database
	 * <li> recomputing the map of trace call path id
	 * <li> removing the filtered nodes to help garbage collector
	 * </ol>  
	 * 
	 * @param experiment
	 * 			The database
	 * @param filter
	 * 			a filter set 
	 * @return
	 * 			the number of filtered nodes
	 * @throws Exception 
	 *************************************************************************/
	private static int filter(Experiment experiment, IFilterData filter) throws Exception
	{
		// ---------------------------------------
		// reopening the database 
		// ---------------------------------------
		experiment.reopen();
		
		if (experiment.getRootScope() == null)
			// case of corrupt database
			return 0;
		
		// ---------------------------------------
		// Since we reopen the database, we need to recompute again the original
		// call path trace map
		// ---------------------------------------
		final RootScope rootCCT = experiment.getRootScope(RootScopeType.CallingContextTree);

		if (experiment.getTraceDataVersion() > 0) {
			
			// needs to gather info about cct id and its depth
			// this is needed for traces
			TraceScopeVisitor visitor = new TraceScopeVisitor();
			rootCCT.dfsVisitScopeTree(visitor);
			
			experiment.setMaxDepth(visitor.getMaxDepth());
			experiment.setScopeMap(visitor.getCallPath());
		}
		// ---------------------------------------------
		// Main action: tree traversal to filter the current cct
		// ---------------------------------------------
		FilterScopeVisitor visitor = new FilterScopeVisitor(rootCCT, filter);
		rootCCT.dfsVisitFilterScopeTree(visitor);

		// ---------------------------------------------
		// Manually free or dispose all resources of elided nodes
		// the reason is that GC is NOT that smart, we need to help it by
		// setting null or empty the variables.
		// ---------------------------------------------
		
		int filterNumScopes = 0;
		
		var listToRemove = visitor.getScopeToRemove();
		if (listToRemove != null && !listToRemove.isEmpty()) {
			filterNumScopes = listToRemove.size();
			listToRemove.stream().forEach( scope -> {
				scope.getParentScope().remove(scope);
				scope.dispose(); 
			});
		}
		var listTreeToRemove = visitor.getTreeToRemove();
		if (listTreeToRemove != null && !listTreeToRemove.isEmpty()) {
			filterNumScopes += listTreeToRemove.size();
			for(var tree: listTreeToRemove) {
				tree.getParentScope().remove(tree);
				tree.disposeSelfAndChildren();
			}
		}

		experiment.setMaxDepth(visitor.getMaxDepth());

		// clean-up
		visitor.dispose();
		
		return filterNumScopes;
	}
}
