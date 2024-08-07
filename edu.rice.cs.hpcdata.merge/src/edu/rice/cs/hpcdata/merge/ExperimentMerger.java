// Copyright 2002-2022 Rice University. All rights reserved.		//
// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcdata.merge;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import edu.rice.cs.hpcdata.db.DatabaseManager;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.ExperimentConfiguration;
import edu.rice.cs.hpcdata.experiment.metric.*;
import edu.rice.cs.hpcdata.experiment.scope.*;
import edu.rice.cs.hpcdata.experiment.scope.visitors.*;


/****
 * Merging experiments
 *
 * Steps:
 * <ul>
 * 	<li>1. create the new experiment 
 * 	<li>2. add metrics 		-->	add into metric list 
 * 	<li>3. add raw metrics 	-->	add into a list
 * 	<li>4. add trace data 	-->	add into a list
 * 	<li>5. merge the experiments
 * </ul>
 */
public class ExperimentMerger 
{
	private static final boolean with_raw_metrics = false;
	
	private ExperimentMerger() {}
	
	/**
	 * Merging two experiments, and return the new experiment
	 * 
	 * @param db
	 * 			List of databases to be merged which includes metrics and
	 * 			type of root scope (top-down or flat). Bottom-up is not 
	 * 			supported at the moment.
	 *  
	 * @return merged experiment database
	 * 
	 * @see DatabasesToMerge
	 */
	public static Experiment merge(DatabasesToMerge db)  {
		
		final String parent_dir = generateMergeName(db.experiment[0], db.experiment[1]);

		return merge(db, parent_dir);
	}
	
	/******
	 * Merging two experiments and specifying the output directory
	 * 
	 * @param db
	 * 			List of databases to be merged 
	 * @param parentDir
	 * 			The parent directory of the new merged database
	 * 
	 * @return the new merged database
	 */
	public static Experiment merge(DatabasesToMerge db, String parentDir) {
		
		Experiment exp1 = db.experiment[0];
		Experiment exp2 = db.experiment[1];
		
		// -----------------------------------------------
		// step 1: create new base Experiment
		// -----------------------------------------------
		Experiment merged = exp1.duplicate();
		
		final ExperimentConfiguration configuration = new ExperimentConfiguration();
		configuration.setName( ExperimentConfiguration.NAME_EXPERIMENT, exp1.getName() + " & " + exp2.getName() );
		//configuration.searchPaths = exp1.getConfiguration().searchPaths;
		
		merged.setConfiguration( configuration );

		// Add tree1, walk tree2 & add; just CCT/Flat
		RootScope rootScope = new RootScope(merged,"Invisible Outer Root Scope", RootScopeType.Invisible);
		merged.setRootScope(rootScope);
				
		// -----------------------------------------------
		// step 2: combine all metrics
		// -----------------------------------------------

		// Bug fix issue #168:
		//  the formula in stat metrics are not computed properly because the hidden metrics
		//	are not merged. We have to merge even the hidden ones :-(				 
		ListMergedMetrics metrics = mergeMetrics(exp1.getMetricList(), exp2.getMetricList());
		merged.setMetrics(metrics);
		
		if (with_raw_metrics)
		{
			final List<BaseMetric> metricRaw = buildMetricRaws( exp1.getRawMetrics(), exp2.getRawMetrics() );
			merged.setMetricRaw(metricRaw);
		}

		// -----------------------------------------------
		// step 3: mark the new experiment file
		// -----------------------------------------------
		
		final var dbName = DatabaseManager.getDatabaseFilename("xml");
		final File fileMerged  = new File( parentDir + File.separator + dbName.orElse("unknown-database")); 
		merged.setXMLExperimentFile( fileMerged );

		// -----------------------------------------------
		// step 4: create a new root by duplicating the tree of experiment 1
		// -----------------------------------------------		
		
		RootScope root1 = exp1.getRootScope(db.type);
		if (root1 == null) {
			throw new IllegalArgumentException("Unable to find root type " + db.type + " in " + exp1.getDefaultDirectory());
		}
		createFlatTree(exp1, db.type, root1);
		
		root1.dfsVisitScopeTree(new DuplicateScopeTreesVisitor(merged, rootScope, 0));
		
		RootScope rootMerged = (RootScope) merged.getRootScopeChildren().get(0);	

		// -----------------------------------------------
		// step 5: merge the two experiments
		// -----------------------------------------------

		RootScope root2 = exp2.getRootScope(db.type);
		if (root2 == null) {
			throw new IllegalArgumentException("Unable to find root type " + db.type + " in " + exp2.getDefaultDirectory());
		}
		createFlatTree(exp2, db.type, root2);

		new TreeSimilarity(metrics.getOffset(), rootMerged, root2, db);
		
		merged.setMergedDatabase();
		
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
	private static ListMergedMetrics mergeMetrics(List<BaseMetric> m1, List<BaseMetric> m2) 
	{
		// ----------------------------------------------------------------
		// step 1: add the first metrics into the merged experiment
		// ----------------------------------------------------------------
		final List<BaseMetric> metricList = m1.stream()
											  .map(BaseMetric::duplicate)
											  .collect(Collectors.toList());
		
		metricList.forEach(metric -> metric.setDisplayName("1-" + metric.getDisplayName()));
		
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
		m1_last_index++;
		metricsMerged.setOffset(m1_last_index);
		
		// ----------------------------------------------------------------
		// step 2: append the second metrics, and reset the index and the key
		// ----------------------------------------------------------------
		List<AbstractMetricWithFormula> listDerivedMetrics = new ArrayList<>();
		Map<Integer, Integer> mapOldIndex = new HashMap<>();
		Map<Integer, Integer> mapOldOrder = new HashMap<>();
		int order = m1_last_order;
		
		for(BaseMetric metric: m2) {
			final BaseMetric m = metric.duplicate();

			if (m instanceof AbstractMetricWithFormula) {
				listDerivedMetrics.add((AbstractMetricWithFormula) m);
			} else {
				// general metric only, no derived metrics allowed
				metricsMerged.add(m);
			}			

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
				order++;
				mapOldOrder.put(m.getOrder(), order);
				m.setOrder(order);
			}			
			mapOldIndex.put(index_old, index_new);
			m.setDisplayName( 2 + "-" + m.getDisplayName() );
		}
		
		// ----------------------------------------------------------------
		// step 3: rename the formula in derived metrics
		// ----------------------------------------------------------------
		if (!listDerivedMetrics.isEmpty()) {
			for(AbstractMetricWithFormula m: listDerivedMetrics) {
				if (m.getMetricType() != MetricType.UNKNOWN) {
					m.renameExpression(mapOldIndex, mapOldOrder);
					metricsMerged.add(m);
				}
			}
		}
		
		return metricsMerged;
	}


	/***
	 * merge two metric raws
	 * 
	 * @param list
	 * @param list2
	 * @return
	 */
	private static List<BaseMetric> buildMetricRaws( List<BaseMetric> list, List<BaseMetric> list2) 
	{
		List<BaseMetric> rawList = new ArrayList<>(list);
		rawList.addAll(list2);
		
		return rawList;
	}
}

