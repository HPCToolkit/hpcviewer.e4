// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcdata.merge;

import java.util.List;

import org.apache.commons.math3.util.Precision;
import org.eclipse.collections.impl.list.mutable.FastList;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.AnnotationType;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.LoopScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.visitors.DuplicateScopeTreesVisitor;
import edu.rice.cs.hpcdata.experiment.scope.visitors.IScopeVisitor;
import edu.rice.cs.hpcdata.experiment.scope.visitors.ResetCounterVisitor;
import edu.rice.cs.hpcdata.tree.ScopeFlatComparator;
import edu.rice.cs.hpcdata.tree.ScopeFlatComparator.SortDirectionEnum;
import java.util.Collections;

/******************************************************
 * 
 * Check similarity of two trees
 * 
 *
 ******************************************************/
public class TreeSimilarity 
{
	public static final String PROP_DEBUG = "merge.debug";
	private static final boolean DEBUG_MODE = Boolean.getBoolean(PROP_DEBUG);
	
	private static final int METRIC_TARGET = 0;
	private static final int METRIC_SOURCE = 1;
	
	int numNodes = 0;
	int numMerges = 0;
	int numUnmerges = 0;
	int numChildMatches = 0;
	int numSiblingMatches = 0;
	
	private enum SimilarityType{ SAME, SIMILAR, DIFF }
	
	private BaseMetric[] metricToCompare;
	
	private final int metricOffset;
	
	/********
	 * construct similarity class
	 * 
	 * @param offset
	 * 			 metric offset
	 * @param target
	 * 			 the target root scope. the target scope has to be a tree,
	 * 			 it cannot be empty
	 * @param source
	 * 			 the source root scope
	 * @param db
	 * 			 The databases to be merged
	 * 
	 */
	public TreeSimilarity(int offset, RootScope target, RootScope source, DatabasesToMerge db)
	{
		metricOffset = offset;
		metricToCompare = db.metric;

		if (db.metric == null || db.metric[0] == null || db.metric[1] == null )
			metricToCompare = DatabasesToMerge.getMetricsToCompare(db.experiment);
		
		// reset counter
		IScopeVisitor visitor = new ResetCounterVisitor();
		source.dfsVisitScopeTree(visitor);
				
		// merge the root scope
		target.accumulateMetrics(source, offset);
		
		// merge the children of the root (tree)
		mergeTree(target.getExperiment(), target, source);
		
		if (DEBUG_MODE) {
			float mergePercent = (float) (numMerges * 100.0 / numNodes);
			float unmergePercent = (float) (numUnmerges * 100.0 / numNodes);
			
			System.out.println("Merged: " + numMerges + "\t " + mergePercent + 
					" %\t Unmerges: " + numUnmerges + " \t" + unmergePercent + " %\t Nodes: "
					+ numNodes + " \t sibling-match: " + numSiblingMatches + 
					"\t child-match: " + numChildMatches);
		}
	}
	
		
	/****
	 * check similarity between 2 trees, and merge them into the
	 * target tree
	 * 
	 * @param target
	 * @param source
	 */
	private void mergeTree(IExperiment targetDatabase, Scope target, Scope source)
	{
		
		// ------------------------------------------------------------
		// case 1: if the source has no children. no need to continue
		// ------------------------------------------------------------
		
		final List<Scope>sortedSource = getSortedChildren( source, metricToCompare[METRIC_SOURCE] );

		if (sortedSource == null)
			return;
		
		// ------------------------------------------------------------
		// case 2: if the target has no children, just add from the source
		// ------------------------------------------------------------
		final List<Scope>sortedTarget = getSortedChildren( target, metricToCompare[METRIC_TARGET] );
		if (sortedTarget == null) 
		{
			for (Scope childSource: sortedSource)
			{
				addSubTree(targetDatabase, target, childSource);
			}
			numUnmerges += sortedSource.size();
			return;
		}
		
		// ------------------------------------------------------------
		// case 3: both target and source have children
		// ------------------------------------------------------------
		
		// 3.a: check for all children in target and source if they are similar
		for (Scope childTarget: sortedTarget) 
		{
			if (childTarget.isCounterZero())
			{
				// ---------------------------------------------------------
				// Algorithm: 
				//	step 1: check the similarity with source's siblings
				//	step 2: if no similarity found, check the kids (inlining case)
				// ---------------------------------------------------------
				
				// step 1: check if one of the child in the source is similar
				for (int i=0; i<sortedSource.size(); i++)
				{	
					Scope childSource = sortedSource.get(i);
					// check if the source has been merged or not
					if (childSource.isCounterZero())
					{
						// check if the scopes are similar
						CoupleNodes candidate = mergeNode(childTarget, childSource, 
								metricToCompare[METRIC_TARGET],
								metricToCompare[METRIC_SOURCE],
								i, sortedSource) ;
						if (candidate != null)
						{
							numMerges += 2;
							
							// DFS: recursively, merge the children if they are similar
							// the recursion will stop when all children are different
							mergeTree( targetDatabase, candidate.target, candidate.source );
							break;
						}
					}
				}					
			}
		}
		
		// 3.b: check for inlined codes on the source part
		checkInlinedScope(targetDatabase, 
						  sortedTarget, 
						  sortedSource, 
						  metricToCompare[METRIC_TARGET], 
						  metricToCompare[METRIC_SOURCE]);
		
		// 3.c: check for inlined codes on the target part
		checkInlinedScope(targetDatabase, 
						  sortedSource, 
						  sortedTarget, 
						  metricToCompare[METRIC_SOURCE], 
						  metricToCompare[METRIC_TARGET]);
		
		// 3.d: add the remainder scopes that are not merged
		for (Scope childSource: sortedSource) 
		{
			if (childSource.isCounterZero())
			{
				addSubTree(targetDatabase, target, childSource);
				numUnmerges++;
			}
		}
		
		for (Scope s: sortedTarget)
		{
			if (s.isCounterZero())
			{
				numUnmerges++;
			}
		}
		numNodes += (sortedSource.size() + sortedTarget.size());
	}
	
	/*****
	 * verify if we can match with the children's scope (inlined cases)
	 * 
	 * @param scope1 : a list of scopes to be compared
	 * @param scope2 : a list of scopes which children are to be compared
	 * @param metricOffset : the metric offset 
	 */
	private void checkInlinedScope(IExperiment targetDatabase,
								   List<Scope> scope1, 
								   List<Scope> scope2, 
								   BaseMetric metric1, 
								   BaseMetric metric2)
	{
		for (int j=0; j<scope1.size(); j++)
		{
			Scope s1 = scope1.get(j);
			if (s1.isCounterZero())
			{
				for (Scope s2: scope2)
				{
					if (s1.isCounterZero() && s2.isCounterZero() && s2.getSubscopeCount()>0)
					{
						List<Scope> sortedGrandChildren = getSortedChildren(s2, metric2);
						for (int i=0; i<sortedGrandChildren.size(); i++)
						{
							Scope ss2 = sortedGrandChildren.get(i);
							if (ss2.isCounterZero() && s1.isCounterZero())
							{
								int numMetric1 = ((Experiment) s1.getExperiment()).getMetricCount();
								int numMetric2 = ((Experiment) ss2.getExperiment()).getMetricCount();
								
								CoupleNodes candidate;
								
								// the scope comparison is oblivious of the order of the scopes.
								// which means comparing s1 and s2 <=> comparing s2 and s2
								// however, the merging is not commutative, i.e. we cannot attach
								//	a "target" scope to the "source" node. 
								// So here, we try to find which one is the target (which is the one
								//	which has the more metrics)
								
								if (numMetric1>numMetric2) 
								{
									// scope1 is the target to merge
									candidate = mergeNode(s1, ss2, metric1, metric2, i, sortedGrandChildren) ;
								} else 
								{
									// scope2 is the target to merge
									candidate = mergeNode(ss2, s1, metric2, metric1, j, scope1);
								}
								if (candidate != null)
								{
									numMerges += 2;
								
									// Fix issue #38: https://github.com/HPCToolkit/hpcviewer/issues/38
									// incorrect inclusive time occurred when propagating metrics from the children to the parent
									// there is no need to do that.
									
/*									// we merged the kid, so we need to merge just the metric to the parent
									Scope parent = candidate.target.getParentScope();
									parent.accumulateMetric(candidate.source, 0, metricOffset, emptyFilter);
									
									// remove the attribution cost of the kid from the parent
									// in theory, we do not need this since we consider the parent is merged if the kid is merged.
									parent = candidate.source.getParentScope();
									disseminateMetric(parent, candidate.source, 0, parent.getMetricValues().size());

									// the kid matches with an inlined code. let's mark the parent to be merged as well
									parent.incrementCounter();
*/									
									// DFS: recursively, merge the children if they are similar
									// the recursion will stop when all children are different
									mergeTree( targetDatabase, candidate.target, candidate.source );
									break;
								}
							}
						}
					}
				}
			}
		}
	}

	
	/****
	 * retrieve the sorted list of the children of a given scope
	 * This function will use a cache if a list is already computed or not
	 * 
	 * @param scope
	 * @return
	 */
	private List<Scope> getSortedChildren(Scope scope, BaseMetric metric)
	{
		var children = scope.getChildren();
		if (children == null)
			return Collections.emptyList();
		
		List<Scope> copyChildren = FastList.newList(children);
		
		copyChildren.sort(new ScopeFlatComparator(metric, SortDirectionEnum.DESCENDING));
		
		return copyChildren;
	}
	
	
	private class CoupleNodes
	{
		Scope target;
		Scope source;
		public CoupleNodes(Scope target, Scope source) 
		{
			this.target = target;
			this.source = source;
		}
	}
	
	
	/****
	 * merge 2 nodes if they have similarity
	 * @param scope1 : target scope
	 * @param scope2 : source scope
	 * @param metric1 the metric of the target scope
	 * @param metric2 the metric of the source scope
	 * @param offsetScope2 : offset of scope 2
	 * @param siblingsScope2 : list of siblings of scope 2
	 * 
	 * @return {@code CoupleNodes}
	 */
	private CoupleNodes mergeNode(
			Scope scope1, 
			Scope scope2, 
			BaseMetric metric1,
			BaseMetric metric2,
			int offsetScope2, 
			List<Scope> siblingsScope2)
	{
		Similarity similar = checkNodesSimilarity( scope1, scope2, metric1, metric2);
		
		if (similar.type == SimilarityType.SAME)
		{
			setMergedNodes(scope1, scope2, metricOffset);
			
			return new CoupleNodes(scope1, scope2);
			
		} else if (similar.type == SimilarityType.SIMILAR) 
		{
			// ---------------------------------------------------------------------
			// Both target and source look similar, but we are not confident enough
			//	Go for source's siblings to see better similarity
			// ---------------------------------------------------------------------
			int nextOffset = offsetScope2 + 1;

			// Looks for only a couple of siblings, we do not need to check
			//	everyone for similarity
			
			int numSiblings = Math.min(siblingsScope2.size()-nextOffset, 
					Constants.MAX_LEN_BFS);
			Scope candidate = scope2;
			
			// check the siblings for scope2
			
			for (int i=nextOffset; i<nextOffset+numSiblings; i++) 
			{
				Scope sibling = siblingsScope2.get(i);
				if (sibling.isCounterZero())
				{
					Similarity result = checkNodesSimilarity(scope1, sibling, metric1, metric2);
					if (result.type == SimilarityType.SAME)
					{
						setMergedNodes(scope1, sibling, metricOffset);
						
						return new CoupleNodes(scope1, sibling);
						
					} else if((result.type == SimilarityType.SIMILAR) &&
							  (result.score > similar.score)) {
						// -------------------------------------------------------------
						// Looks similar, and the sibling has a better score
						// -------------------------------------------------------------
						similar = result;
						candidate = sibling;
					}
				}
			}
			numSiblingMatches++;
			setMergedNodes(scope1, candidate, metricOffset);
			
			return new CoupleNodes(scope1, candidate);
		}
		return null;
	}
	
	
	private void setMergedNodes(Scope target, Scope source, int offset)
	{
		assert target.isCounterZero() : "target counter is not zero: " + target ;
		assert source.isCounterZero() : "source counter is not zero: " + source ;
		
		// -------------------------------------------------------------
		// Found strong similarity in the sibling: merge the metric
		// -------------------------------------------------------------
		target.accumulateMetrics(source, offset);
		
		// mark the nodes have been merged
		source.incrementCounter();
		target.incrementCounter();
	}
	
	
	/***
	 * check similarity between two scopes without checking the children
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private int getScopeSimilarityScore ( Scope s1, Scope s2, BaseMetric m1, BaseMetric m2 )
	{
		// check the adjacency of the location
		int loc_distance = 0;
		
		// we can only compare the location if both has the information of
		// line number. Otherwise, we just gives up.
		
		if (s1.getFirstLineNumber() > 0 && s2.getFirstLineNumber() > 0)
			Math.abs(s1.getFirstLineNumber() - s2.getFirstLineNumber());
		
		// check the type
		final boolean same_type = areSameType( s1, s2 );
		
		// check the metrics
		final var metric_distance = getMetricDistance( s1, s2, m1, m2 );

		// check if it's the same name
		final boolean same_name = areSameName( s1, s2 );
		
		int score = (int) (Constants.SCORE_INIT + metric_distance);
		
		int score_loc = (int) Math.max(Constants.WEIGHT_LOCATION, loc_distance * Constants.WEIGHT_LOCATION_COEF);
		
		score -= score_loc;

		// hack: if both s1 and s2 are call sites, their name should be the same
		//	we need to be careful with false positive where similar metric, with similar children 
		//	and similar line number with different function name happens
		if (same_type && (s1 instanceof CallSiteScope))
		{
			score += (same_name? 3:-1) * Constants.WEIGHT_NAME;
		} else
		{
			score += (same_name ? Constants.WEIGHT_NAME : 0);
		}
		
		if (isOnlyChild( s1, s2 ))
			score += Constants.WEIGHT_LOCATION;
		
		score += (same_type ? Constants.WEIGHT_TYPE : 0);
		
		return score;
	}
	
	private boolean isOnlyChild( Scope s1, Scope s2 )
	{
		int ns1 = s1.getParentScope().getSubscopeCount();
		int ns2 = s2.getParentScope().getSubscopeCount();
		
		return (ns1==ns2 && ns1==1);
	}
	
	/****
	 * verify if 2 scopes are exactly the same, almost similar, or completely different.
	 * the highest the score of similarity, the more likely they are similar
	 * 
	 * @param s1
	 * @param s2
	 * @return similarity of the two scopes
	 */
	private Similarity checkNodesSimilarity( Scope s1, Scope s2, BaseMetric m1, BaseMetric m2 )
	{
		Similarity result = new Similarity();
		result.type = SimilarityType.DIFF;
		result.score = getScopeSimilarityScore( s1, s2, m1, m2 );
		
		// check if the children are the same
		result.score += getChildrenSimilarityScore( s1, s2, m1, m2 );

		if (result.score>Constants.SCORE_SAME)
		{
			// we are confident enough that the two scopes are similar
			result.type = SimilarityType.SAME;
		}
		else if (result.score>Constants.SCORE_SIMILAR)
		{
			// we are not confident, but it look like they are similar
			// in this case, the caller has to check if other combinations exist
			result.type = SimilarityType.SIMILAR;
		}
		
		if (DEBUG_MODE)
		{
			System.out.println("TS " + s1 + " [" + s1.getCCTIndex()+"] \tvs.\t " +s2  + " ["+ s2.getCCTIndex()
					+"]\t s: " + result.score +"\t t: " + result.type);
		}
		return result;
	}

	/**
	 * check if the name of two scopes are similar 
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private boolean areSameName( Scope s1, Scope s2 )
	{
		return s1.getName().equals(s2.getName());
	}
	
	private boolean areSameType( Scope s1, Scope s2)
	{
		final Class<? extends Scope> c1 = s1.getClass();
		final Class<? extends Scope> c2 = s2.getClass();
		
		return (c1 == c2); 
	}
	
	private double getMetricDistance( Scope s1, Scope s2, BaseMetric m1, BaseMetric m2 )
	{
		int multiplier = Constants.WEIGHT_METRIC;
		
		final var v1 = getAnnotationValue(s1, m1);
		final var v2 = getAnnotationValue(s2, m2);
		if (v1 > .5 && v2 > .5)
			multiplier *= 2;
		var distance = 1 - Math.abs(v2-v1);
		return (distance * multiplier);
	}
	

	/****
	 * check if two scopes have the same children
	 * we just use 2 simple properties: 
	 * - it is the same if the number of children are the same, or
	 * - if at least once child is exactly the same
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private boolean areSameChildren(Scope s1, Scope s2, BaseMetric metric1, BaseMetric metric2, int currDepth)
	{
		// check if we go too far or not
		if (currDepth > Constants.MAX_LEN_DFS)
			return false;
		
		// both should have the same children
		if (s1.getSubscopeCount()==0 || s2.getSubscopeCount()==0)
			return false;
		
		final List<Scope> sortedS1 = getSortedChildren(s1, metric1);
		final List<Scope> sortedS2 = getSortedChildren(s2, metric2);

		// we only check with limited number of children
		// there's no need to check all children
		int c1 = Math.min( Constants.MAX_LEN_BFS, sortedS1.size() );
		int c2 = Math.min( Constants.MAX_LEN_BFS, sortedS2.size() );
		
		int finalScore = 0;
		
		// is there a child that is exactly the same ?
		for (int i=0; i<c1 ; i++)
		{
			final Scope cs1 = sortedS1.get(i);
			
			for (int j=0; j<c2; j++) 
			{
				final Scope cs2 = sortedS2.get(j);
				int score = getScopeSimilarityScore( cs1, cs2, metric1, metric2 );
				
				if (score > Constants.SCORE_SIMILAR)
					finalScore += 3;
				else 
				{ 	// not the same nodes: check their descendants
					// check if one (or all) of them are aliens
					final boolean cs1_is_alien = isAlien( cs1 );
					final boolean cs2_is_alien = isAlien( cs2 );
					Scope next1 = s1, next2 = s2;
					
					// for alien procedure: we will go deep to their children
					//	the number of depth will be limited to MAX_LEN_DFS
					if (cs1_is_alien)
						next1 = cs1;
					if (cs2_is_alien)
						next2 = cs2;
					
					if (cs1_is_alien || cs2_is_alien)
					{
						if (areSameChildren(next1, next2, metric1, metric2, currDepth + 1))
							finalScore++;
					} else
					{						
						final boolean areLoops = (cs1 instanceof LoopScope && cs2 instanceof LoopScope);
						if (areLoops && areSameChildren( cs1, cs2, metric1, metric2, currDepth + 1))
						{
							finalScore++;
						}
					}
				}
			}
		}
		return (finalScore >= Math.max(c1, c2));
	}
	
	/***
	 * return true if the scope is an alien
	 * 
	 * @param scope
	 * @return true if the scope is an alien
	 */
	private boolean isAlien( Scope scope )
	{
		if (scope instanceof ProcedureScope)
		{
			return ((ProcedureScope) scope).isAlien();
		}
		return false;
	}
	
	/****
	 * return the score for children similarity
	 * 
	 * @param s1 {@code Scope}
	 * @param s2 {@code Scope}
	 * @param m1 {@code BaseMetric}
	 * @param m2 {@code BaseMetric}
	 * @return int scope
	 */
	private int getChildrenSimilarityScore( Scope s1, Scope s2, BaseMetric m1, BaseMetric m2 )
	{
		final boolean is_same = areSameChildren( s1, s2, m1, m2, 0 );
		int score = 0;
		if (is_same)
		{
			score = Constants.WEIGHT_CHILDREN;
		}
		return score;
	}
	
	/****
	 * find the "value" of a scope. We expect a value to be a relative value
	 * of a scope, compared its siblings. This can be a percentage, or others.
	 * 
	 * @param s
	 * @return
	 */
	private double getAnnotationValue(Scope s, BaseMetric m)
	{
		final MetricValue mv = m.getValue(s);

		if (m.getAnnotationType() == AnnotationType.PERCENT ||
			m.getAnnotationType() == AnnotationType.PERCENT_COLOR_BAR) {
			
			final MetricValue mvRoot = s.getRootScope().getMetricValue(m);
			
			final var rv = mvRoot.getValue();
			final var epsilon = Math.ulp(rv);

			if (!Precision.equals(rv, 0.0, epsilon)) {
				var v = mv.getValue();
				return v/rv;
			}
		}
		// no annotation is available. Should we return the value ?
		return mv.getValue();
	}
	
	/****
	 * recursively add subtree (and the metrics) to the parent
	 * @param parent : parent target
	 * @param node : source nodes to be copied
	 * @param metricOffset : offset of the metric
	 */
	private void addSubTree(IExperiment targetDatabase, Scope parent, Scope node)
	{
		DuplicateScopeTreesVisitor visitor = new DuplicateScopeTreesVisitor(targetDatabase, parent, metricOffset);
		node.dfsVisitScopeTree(visitor);
	}
	
	
	
	private class Similarity 
	{
		SimilarityType type;
		int score;
	}
}