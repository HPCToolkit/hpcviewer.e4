//////////////////////////////////////////////////////////////////////////
//																		//
//	ExperimentMerger.java												//
//																		//
//	ExperimentMerger -- class to merge two Experiments					//
//	Created: May 7, 2007 												//
//																		//
//	(c) Copyright 2007-2012 Rice University. All rights reserved.		//
//																		//
//////////////////////////////////////////////////////////////////////////
package edu.rice.cs.hpcdata.experiment.merge;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.ExperimentConfiguration;
import edu.rice.cs.hpcdata.experiment.metric.*;
import edu.rice.cs.hpcdata.experiment.scope.*;
import edu.rice.cs.hpcdata.experiment.scope.visitors.*;
import edu.rice.cs.hpcdata.util.Constants;

/****
 * Merging experiments
 *
 * Steps:
 * 
 * 1. create the new experiment 
 * 2. add metrics 		-->	add into metric list 
 * 3. add raw metrics 	-->	add into a list
 * 4. add trace data 	-->	add into a list
 * 5. merge the experiments
 */
public class ExperimentMerger 
{
	static final private boolean with_raw_metrics = false;
	
	/**
	 * Merging two experiments, and return the new experiment
	 * 
	 * @param exp1 : first database 
	 * @param exp2 : second database
	 * @param type : root to merge (cct, bottom-up tree, or flat tree)
	 *  
	 * @return merged experiment database
	 * @throws Exception 
	 */
	static public Experiment merge(Experiment exp1, Experiment exp2, RootScopeType type) throws Exception {
		
		final String parent_dir = generateMergeName(exp1, exp2);

		return merge(exp1, exp2, type, parent_dir);
	}
	
	/******
	 * Merging two experiments and specifying the output directory
	 * 
	 * @param exp1
	 * @param exp2
	 * @param type
	 * @param parent_dir
	 * @return the new merged database
	 * @throws Exception 
	 */
	static public Experiment merge(Experiment exp1, Experiment exp2, RootScopeType type, 
			String parent_dir) throws Exception {
		
		// -----------------------------------------------
		// step 1: create new base Experiment
		// -----------------------------------------------
		Experiment merged = exp1.duplicate();
		
		final ExperimentConfiguration configuration = new ExperimentConfiguration();
		configuration.setName( ExperimentConfiguration.NAME_EXPERIMENT, exp1.getName() + " & " + exp2.getName() );
		configuration.searchPaths = exp1.getConfiguration().searchPaths;
		
		merged.setConfiguration( configuration );

		// Add tree1, walk tree2 & add; just CCT/Flat
		RootScope rootScope = new RootScope(merged,"Invisible Outer Root Scope", RootScopeType.Invisible);
		merged.setRootScope(rootScope);
				
		// -----------------------------------------------
		// step 2: combine all metrics
		// -----------------------------------------------
		ListMergedMetrics metrics = buildMetricList(merged, exp1.getMetricList(), exp2.getMetricList());
		merged.setMetrics(metrics);
		
		if (with_raw_metrics)
		{
			final List<MetricRaw> metricRaw = buildMetricRaws( exp1.getMetricRaw(), exp2.getMetricRaw() );
			merged.setMetricRaw(metricRaw);
		}

		// -----------------------------------------------
		// step 3: mark the new experiment file
		// -----------------------------------------------

		final File fileMerged  = new File( parent_dir + File.separator + Constants.DATABASE_FILENAME); 
		merged.setXMLExperimentFile( fileMerged );

		// -----------------------------------------------
		// step 4: create a new root by duplicating the tree of experiment 1
		// -----------------------------------------------		
		
		RootScope root1 = exp1.getRootScope(type);
		if (root1 == null) {
			throw new Exception("Unable to find root type " + type + " in " + exp1.getDefaultDirectory());
		}
		createFlatTree(exp1, type, root1);
		
		root1.dfsVisitScopeTree(new DuplicateScopeTreesVisitor(rootScope, 0));
		
		RootScope rootMerged = (RootScope) merged.getRootScopeChildren()[0];	

		// -----------------------------------------------
		// step 5: merge the two experiments
		// -----------------------------------------------

		RootScope root2 = exp2.getRootScope(type);
		if (root2 == null) {
			throw new Exception("Unable to find root type " + type + " in " + exp2.getDefaultDirectory());
		}
		createFlatTree(exp2, type, root2);

		new TreeSimilarity(metrics.getOffset(), rootMerged, root2);
		
		merged.setMergedDatabase(true);
		
		return merged;
	}

	/****
	 * generate a "virtual" merge experiment name
	 * 
	 * @param exp1
	 * @param exp2
	 * @return
	 */
	public static String generateMergeName(Experiment exp1, Experiment exp2)  
	{
		boolean need_to_find_name = true;
		File dir  = exp1.getDefaultDirectory();
		File path;

		// find a unique name for the merged file
		do {
			String tmpDir = Long.toString(System.nanoTime());
			path = new File(dir + File.separator + tmpDir);
			
			// normally there is no way two executions at the same nano second
			// but I don't want to take a risk
			need_to_find_name = path.exists();
		} while (need_to_find_name);
		
		return path.getAbsolutePath();
	}
	
	
	/*******
	 * create flat tree if needed. <br/>
	 * The latest version of hpcviewer, flat tree is generated dynamically when needed.
	 * For merging, we need to force to create the flat trees and then merge them.
	 * 
	 * @param exp
	 * @param type
	 * @param rootFlat
	 * @return
	 */
	private static RootScope createFlatTree(Experiment exp, RootScopeType type, RootScope rootFlat)
	{
		if (!rootFlat.hasChildren() && type == RootScopeType.Flat) {
			// create flat tree if it is not created yet
			final RootScope callingContextViewRootScope = exp.getRootScope(RootScopeType.CallingContextTree);
			rootFlat = exp.createFlatView(callingContextViewRootScope, rootFlat);
		}
		return rootFlat;
	}
	
	/***
	 * combine metrics from list m1 and m2 to experiment exp
	 * 
	 * @param exp target experiment
	 * @param m1 first metric list
	 * @param m2 second metric list
	 * @return the new metric list that has been set to experiment exp
	 */
	private static ListMergedMetrics buildMetricList(Experiment exp, List<BaseMetric> m1, List<BaseMetric> m2) 
	{
		// ----------------------------------------------------------------
		// step 1: add the first metrics into the merged experiment
		// ----------------------------------------------------------------
		final List<BaseMetric> metricList = m1.stream().
											   map(metric -> metric.duplicate()).
											   collect(Collectors.toList());
		metricList.forEach(metric -> {
			metric.setDisplayName("1-" + metric.getDisplayName());
		});
		
		// attention: hpcprof doesn't guarantee the ID of metric starts with zero
		//	we should make sure that the next ID for the second group of metrics is not
		//	overlapped with the ID from the first group of metrics
		
		int m1_last_index = -1;
		int m1_last_order = -1;
		
		for (BaseMetric m : m1) {
			m1_last_index = Math.max(m1_last_index, m.getIndex());
			m1_last_order = Math.max(m1_last_order, m.getOrder());
		}
		
		ListMergedMetrics metricsMerged = new ListMergedMetrics(metricList);
		
		metricsMerged.setOffset(m1_last_index);
		
		// ----------------------------------------------------------------
		// step 2: append the second metrics, and reset the index and the key
		// ----------------------------------------------------------------
		List<DerivedMetric> listDerivedMetrics = new ArrayList<>();
		Map<Integer, Integer> mapOldIndex = new HashMap<>();
		Map<Integer, Integer> mapOldOrder = new HashMap<>();
		int order = m1_last_order;
		
		for(BaseMetric metric: m2) {
			final BaseMetric m = metric.duplicate();
			order++;

			if (m instanceof DerivedMetric) {
				listDerivedMetrics.add((DerivedMetric) m);
			} else {
				// general metric only, no derived metrics allowed

				// change the short name (or ID) of the metric since the old ID is already
				// taken by previous experiment
				final int index_old = m.getIndex();
				final int index_new = m1_last_index + index_old;
				final String new_id = String.valueOf(index_new); 
				m.setShortName( new_id );
				m.setIndex(index_new);
				
				// set the partner index
				int partner = m1_last_index + m.getPartner();
				m.setPartner(partner);
				
				// set the new order
				if (m.getOrder() >= 0) {
					mapOldOrder.put(m.getOrder(), order);
					m.setOrder(order);
				}
				
				metricsMerged.add(m);
				mapOldIndex.put(index_old, index_new);
			}			
			m.setDisplayName( 2 + "-" + m.getDisplayName() );
		}
		
		// ----------------------------------------------------------------
		// step 2b: rename the formula in derived metrics
		// ----------------------------------------------------------------
		if (listDerivedMetrics.size()>0) {
			for(DerivedMetric m: listDerivedMetrics) {
				m.renameExpression(mapOldIndex, mapOldOrder);
				metricsMerged.add(m);
			}
		}
		
		// ----------------------------------------------------------------
		// step 3: set the list of metric into the experiment
		// ----------------------------------------------------------------
		exp.setMetrics(metricsMerged);
		
		return metricsMerged;
	}


	/***
	 * merge two metric raws
	 * 
	 * @param raws1
	 * @param raws2
	 * @return
	 */
	private static List<MetricRaw> buildMetricRaws( List<MetricRaw> raws1, List<MetricRaw> raws2) 
	{
		List<MetricRaw> rawList = new ArrayList<>(raws1);
		rawList.addAll(raws2);
		
		return rawList;
	}
}

