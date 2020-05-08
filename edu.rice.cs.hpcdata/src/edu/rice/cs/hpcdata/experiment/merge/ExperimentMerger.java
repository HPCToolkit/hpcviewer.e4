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
	 * @param verbose : true if the verbose mode is on
	 *  
	 * @return
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
	 * @param verbose
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
		List<BaseMetric> metrics = buildMetricList(merged, exp1.getMetrics(), exp2.getMetrics());
		merged.setMetrics(metrics);
		
		if (with_raw_metrics)
		{
			final MetricRaw metricRaw[] = buildMetricRaws( exp1.getMetricRaw(), exp2.getMetricRaw() );
			merged.setMetricRaw(metricRaw);
		}

		// -----------------------------------------------
		// step 3: mark the new experiment file
		// -----------------------------------------------

		final File fileMerged  = new File( parent_dir + File.separator + Constants.DATABASE_FILENAME); 
		merged.setXMLExperimentFile( fileMerged );

		// -----------------------------------------------
		// step 4: create the root for the 
		// -----------------------------------------------		
		
		RootScope root1 = exp1.getRootScope(type);
		if (root1 == null) {
			throw new Exception("Unable to find root type " + type + " in " + exp1.getDefaultDirectory());
		}
		createFlatTree(exp1, type, root1);
		
		root1.dfsVisitScopeTree(new DuplicateScopeTreesVisitor(rootScope));
		
		RootScope rootMerged = (RootScope) merged.getRootScopeChildren()[0];	

		// -----------------------------------------------
		// step 5: merge the two experiments
		// -----------------------------------------------

		RootScope root2 = exp2.getRootScope(type);
		if (root2 == null) {
			throw new Exception("Unable to find root type " + type + " in " + exp2.getDefaultDirectory());
		}
		createFlatTree(exp2, type, root2);

		final int metricCount = exp1.getMetricCount();
		new TreeSimilarity(metricCount, rootMerged, root2);
		
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
	 * combine metrics from exp 1 and exp 2
	 * 
	 * @param exp
	 * @param m1
	 * @param m2
	 * @return
	 */
	private static ArrayList<BaseMetric> buildMetricList(Experiment exp, BaseMetric[] m1, BaseMetric[] m2) 
	{
		final ArrayList<BaseMetric> metricList = new ArrayList<BaseMetric>( m1.length + m2.length );
		
		// ----------------------------------------------------------------
		// step 1: add the first metrics into the merged experiment
		// ----------------------------------------------------------------
		for (int i=0; i<m1.length; i++) {
			// add metric into the merged list
			BaseMetric mm = m1[i].duplicate();
			addMetric(mm, i, exp, 1, metricList);
		}
		
		// attention: hpcprof doesn't guarantee the ID of metric starts with zero
		//	we should make sure that the next ID for the second group of metrics is not
		//	overlapped with the ID from the first group of metrics
		final int m1_last = m1.length - 1;
		final int m1_last_shortname = Integer.valueOf(m1[m1_last].getShortName());
		int m1_last_index = Math.max(m1_last_shortname, m1[m1_last].getIndex()) + 1;
		
		// ----------------------------------------------------------------
		// step 2: append the second metrics, and reset the index and the key
		// ----------------------------------------------------------------
		
		for (int i=0; i<m2.length; i++) {
			final BaseMetric m = m2[i].duplicate();
			
			if (!(m instanceof DerivedMetric)) {
				// general metric only, no derived metrics allowed

				// change the short name (or ID) of the metric since the old ID is already
				// taken by previous experiment
				final int index_new = m1_last_index + m.getIndex();
				final String new_id = String.valueOf(index_new); 
				m.setShortName( new_id );
				
				addMetric(m, m1_last + i +1, exp, 2, metricList);
			}
		}
		
		// ----------------------------------------------------------------
		// step 3: set the list of metric into the experiment
		// ----------------------------------------------------------------
		exp.setMetrics(metricList);
		
		return metricList;
	}


	/*********
	 * Add a new metric into a metric list
	 * 
	 * @param source : metric to duplicate
	 * @param metric_index  : the new index
	 * @param exp    : experiment to which the metric is hosted
	 * @param metricList : the list of metrics
	 */
	private static BaseMetric addMetric(BaseMetric source, int metric_index, 
			Experiment exp, int experiment_index, List<BaseMetric> metricList)
	{
		// add metric into the merged list
		BaseMetric mm = source.duplicate();
		mm.setIndex(metric_index);
		// update the derived metric's experiment
		if (mm instanceof DerivedMetric)
		{
			((DerivedMetric)mm).setExperiment(exp);
		}
		
		setMetricCombinedName(experiment_index, mm);
		metricList.add(mm);
		return mm;
	}
	
	
	/***
	 * merge two metric raws
	 * 
	 * @param raws1
	 * @param raws2
	 * @return
	 */
	private static MetricRaw[] buildMetricRaws( BaseMetric raws1[], BaseMetric raws2[]) 
	{
		MetricRaw rawList[] = new MetricRaw[ raws1.length + raws2.length ];
		
		for (int i=0; i<raws1.length; i++)
		{
			rawList[i] = (MetricRaw) raws1[i].duplicate();
			setMetricCombinedName(1, rawList[i]);
		}
		
		for (int i=0; i<raws2.length; i++)
		{
			rawList[i + raws1.length] = (MetricRaw) raws2[i].duplicate();
			setMetricCombinedName(2, rawList[i + raws1.length]);
		}
		
		return rawList;
	}

	/***
	 * create a new metric name based on the offset of the experiment and the metric
	 * 
	 * @param offset
	 * @param m
	 */
	private static void setMetricCombinedName( int offset, BaseMetric m )
	{
		m.setDisplayName( offset + "-" + m.getDisplayName() );
	}
}

