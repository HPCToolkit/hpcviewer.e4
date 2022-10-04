//////////////////////////////////////////////////////////////////////////
//																		//
//	Experiment.java														//
//																		//
//	experiment.Experiment -- an open HPCView experiment					//
//	Last edited: January 15, 2002 at 12:37 am							//
//																		//
//	(c) Copyright 2002-2022 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpcdata.experiment;


import edu.rice.cs.hpcdata.experiment.metric.*;
import edu.rice.cs.hpcdata.experiment.scope.*;
import edu.rice.cs.hpcdata.experiment.scope.filters.*;
import edu.rice.cs.hpcdata.experiment.scope.visitors.*;
import edu.rice.cs.hpcdata.filter.IFilterData;
import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpcdata.util.IUserData;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//	CLASS EXPERIMENT													//
//////////////////////////////////////////////////////////////////////////

/**
 *
 * An HPCView experiment and its data.
 *
 */


public class Experiment extends BaseExperimentWithMetrics
{
	static public enum ExperimentOpenFlag {TREE_CCT_ONLY, TREE_ALL, TREE_MERGED};
	
	static final public String TITLE_TOP_DOWN_VIEW    = "Top-down view";
	static final public String TITLE_BOTTOM_UP_VIEW   = "Bottom-up view";
	static final public String TITLE_FLAT_VIEW 		  = "Flat view";
	static final public String TITLE_DATACENTRIC_VIEW = "Datacentric view";
	
	/** 
	 * List of raw metrics for thread level database.<br/>
	 * This is only available for the old dense metric format.
	 * For the new format, we don't need this, but still the caller
	 * needs to set the raw metrics to ensure the compatibility :-(
	 * <br/>
	 */
	private List<BaseMetric>  metrics_raw;
	
	/***
	 * Any flag about this experiment database.
	 */
	private ExperimentOpenFlag flag;
	
	private final List<HierarchicalMetric> rootMetrics = new ArrayList<>(0);

	//////////////////////////////////////////////////////////////////////////
	//	hierarchy of metrics												//
	//////////////////////////////////////////////////////////////////////////

	public void addRootMetric(HierarchicalMetric metric) {
		rootMetrics.add(metric);
	}
	
	
	public int getRootMetricCount() {
		return rootMetrics.size();
	}
	
	public HierarchicalMetric getRootMetric(int index) {
		return rootMetrics.get(index);
	}
	
	public Iterator<HierarchicalMetric> getRootMetricIterator() {
		return rootMetrics.iterator();
	}

	//////////////////////////////////////////////////////////////////////////
	// Case for merged database
	//////////////////////////////////////////////////////////////////////////
	
	public void setMergedDatabase()
	{
		this.flag = ExperimentOpenFlag.TREE_MERGED;
	}
	
	public boolean isMergedDatabase()
	{
		return this.flag == ExperimentOpenFlag.TREE_MERGED;
	}
	
	//////////////////////////////////////////////////////////////////////////
	// File opening															//
	//////////////////////////////////////////////////////////////////////////

	/**
	 * Open a database and specify what kind of the database it is. 
	 * 
	 * @see edu.rice.cs.hpcdata.experiment.Experiment.ExperimentOpenFlag
	 * @see edu.rice.cs.hpc.data.experiment.BaseExperiment#open(java.io.File, edu.rice.cs.hpc.data.util.IUserData, boolean)
	 */
	public void open(File fileExperiment, IUserData<String, String> userData, ExperimentOpenFlag flag)
			throws	Exception
	{
		this.flag = flag;
		super.open(fileExperiment, userData, true);
	}

	//////////////////////////////////////////////////////////////////////////
	// Postprocessing														//
	//////////////////////////////////////////////////////////////////////////
	protected void accumulateMetricsFromKids(Scope target, Scope source, MetricValuePropagationFilter filter) {
		int nkids = source.getSubscopeCount();
		for (int i = 0; i < nkids; i++) {
			Scope child = source.getSubscope(i);
			if (child instanceof LoopScope) {
				accumulateMetricsFromKids(target, child, filter);
			}
			target.accumulateMetrics(child, filter, this.getMetricCount());
		}
	}

	
	/*************************************************************************
	 *	Adds a new scope subtree to the scope tree (& scope list)
	 ************************************************************************/
	public void beginScope(Scope scope)
	{
		Scope top = this.getRootScope();
		top.addSubscope(scope);
		scope.setParentScope(top);
	}

	/***
	 * Preparing the tree for caller view. Since we will create the tree dynamically,
	 * 	we need to create at least the root. All the children will be created by
	 * 	createCallersView() method.
	 * 
	 * @param callingContextViewRootScope
	 * @return
	 */
	protected RootScope prepareCallersView(Scope callingContextViewRootScope)
	{
		RootScope callersViewRootScope = new RootScope(this, Experiment.TITLE_BOTTOM_UP_VIEW, RootScopeType.CallerTree);
		beginScope(callersViewRootScope);

		// bug fix 2008.10.21 : we don't need to recompute the aggregate metrics here. Just copy it from the CCT
		//	This will solve the problem where there is only nested loops in the programs
		callingContextViewRootScope.copyMetrics(callersViewRootScope, 0);
		
		return callersViewRootScope;
	}
	
	/***
	 * create callers view
	 * @param callingContextViewRootScope
	 * @return
	 */
	public RootScope createCallersView(Scope callingContextViewRootScope, RootScope callersViewRootScope)
	{
		EmptyMetricValuePropagationFilter filter = new EmptyMetricValuePropagationFilter();

		CallersViewScopeVisitor csv = new CallersViewScopeVisitor(this, callersViewRootScope, filter);
		callingContextViewRootScope.dfsVisitScopeTree(csv);
		
		return callersViewRootScope;
	}

	/******
	 * Preparing the tree of the flat view. This method will create just the root.
	 * <br/>The caller is responsible to call {@link createFlatView} method to generate
	 * the real tree.
	 * 
	 * @param cctRootScope : CCT root
	 * @return the root of flat tree
	 */
	private RootScope prepareFlatView(Scope cctRootScope) 
	{
		//final int RANDOM_NUMBER = 12345;
		RootScope flatRootScope = new RootScope(this, 
												Experiment.TITLE_FLAT_VIEW, 
												RootScopeType.Flat);
		beginScope(flatRootScope);

		// bug fix 2008.10.21 : we don't need to recompute the aggregate metrics here. Just copy it from the CCT
		//	This will solve the problem where there is only nested loops in the programs
		cctRootScope.copyMetrics(flatRootScope, 0);
		
		return flatRootScope;
	}
	
	/***
	 * Create a flat tree
	 * 
	 * @param callingContextViewRootScope : the original CCT 
	 * @return the root scope of flat tree
	 */
	public RootScope createFlatView(Scope callingContextViewRootScope, RootScope flatViewRootScope)
	{
		IScopeVisitor fv = getMajorVersion() == Constants.EXPERIMENT_DENSED_VERSION ?
									new FlatViewScopeVisitor(this, (RootScope) flatViewRootScope) :
									new FlatViewScopeVisitor4(flatViewRootScope);

		callingContextViewRootScope.dfsVisitScopeTree(fv);
		return flatViewRootScope;
	}

	/****
	 * Prepare and create datacentric root (if exists)
	 * <p>This method has to be called after computing the CCT's inclusive metrics  
	 ****/
	private void prepareDatacentricView()
	{
		if (datacentricRootScope != null) {

			// include the datacentric root into a child of "invisible root"
			beginScope(datacentricRootScope);

			if (inclusiveNeeded()) {
				// TODO: if the metric is a derived metric then DO NOT do this process !
				InclusiveOnlyMetricPropagationFilter rootInclProp = new InclusiveOnlyMetricPropagationFilter(this);
				addInclusiveMetrics(datacentricRootScope, rootInclProp);
				computeExclusiveMetrics(datacentricRootScope);
			}
			// copy the aggregate of inclusive metric to the exclusive ones 
			EmptyMetricValuePropagationFilter emptyFilter = new EmptyMetricValuePropagationFilter();
			copyMetricsToPartner(datacentricRootScope, MetricType.INCLUSIVE, emptyFilter);
		}
	}

	private void addInclusiveMetrics(Scope scope, MetricValuePropagationFilter filter)
	{
		InclusiveMetricsScopeVisitor isv = new InclusiveMetricsScopeVisitor(this, filter);
		scope.dfsVisitScopeTree(isv);
	}

	private void computeExclusiveMetrics(Scope scope) {
		ExclusiveCallingContextVisitor visitor = new ExclusiveCallingContextVisitor(this);
		scope.dfsVisitScopeTree(visitor);
	}



	/**
	 * Post-processing for CCT:
	 * <p>
	 * <ol><li>Step 1: normalizing CCT view
	 *  <ul><li> normalize line scope, which means to add the cost of line scope into call site scope
	 *  	<li> compute inclusive metrics for I
	 *  	<li> compute inclusive metrics for X
	 *  </li>
	 *  </ul>
	 *  <li>Step 2: create call view (if enabled) and flat view
	 *  <li>Step 3: finalize metrics for cct and flat view (callers view metric will be finalized dynamically)
	 * </ol></p>
	 * @param callerView : flag whether to compute caller view (if true) or not.
	 */
	private void postprocess(boolean callerView) {
		if (rootScope == null)
			// case of corrupt file
			throw new RuntimeException("The database is empty or corrupt");
		
		if (this.rootScope.getSubscopeCount() <= 0) return;
		
		// Get first scope subtree: CCT or Flat
		Scope firstSubTree = this.rootScope.getSubscope(0);
		if (!(firstSubTree instanceof RootScope)) 
			return;
		
		RootScopeType firstRootType = ((RootScope)firstSubTree).getType();

		if (firstRootType.equals(RootScopeType.CallingContextTree)) {
			// accumulate, create views, percents, etc
			Scope callingContextViewRootScope = firstSubTree;

			//----------------------------------------------------------------------------------------------
			// Inclusive metrics
			//----------------------------------------------------------------------------------------------
			if (inclusiveNeeded()) {
				// TODO: if the metric is a derived metric then DO NOT do this process !
				InclusiveOnlyMetricPropagationFilter rootInclProp = new InclusiveOnlyMetricPropagationFilter(this);
				addInclusiveMetrics(callingContextViewRootScope, rootInclProp);
				computeExclusiveMetrics(callingContextViewRootScope);
			}

			//----------------------------------------------------------------------------------------------
			// copy the value of inclusive metrics to exclusive metrics
			//----------------------------------------------------------------------------------------------
			EmptyMetricValuePropagationFilter emptyFilter = new EmptyMetricValuePropagationFilter();
			copyMetricsToPartner(callingContextViewRootScope, MetricType.INCLUSIVE, emptyFilter);
			
			//----------------------------------------------------------------------------------------------
			// Callers View
			//----------------------------------------------------------------------------------------------
			// since we create the caller tree lazily, there is no harm to create the root in the beginning
			prepareCallersView(callingContextViewRootScope);

			//----------------------------------------------------------------------------------------------
			// Flat View
			//----------------------------------------------------------------------------------------------
			// While creating the root of flat tree, we attribute the cost for procedure scopes
			// One the tree has been created, we compute the inclusive cost for other scopes
			prepareFlatView(callingContextViewRootScope);
			
			//----------------------------------------------------------------------------------------------
			// Datacentric View
			//----------------------------------------------------------------------------------------------
			prepareDatacentricView();

		}
		hideEmptyMetrics(firstSubTree);
	}


	/***
	 * Clone the this experiment including the configuration 
	 */
	public Experiment duplicate() {

		Experiment copy 	= new Experiment();
		copy.configuration 	= configuration;
		copy.databaseRepresentation =  databaseRepresentation.duplicate();
		
		return copy;
	}


	/****
	 * Set the XML file
	 * 
	 * @param file
	 */
	public void setXMLExperimentFile(File file) {
		databaseRepresentation.setFile(file);
	}

	
	/***
	 * Set the list of metric raw.
	 * 
	 * @param metricRawList MetricRaw []
	 */
	public void setMetricRaw(List<BaseMetric> metricRawList) {
		metrics_raw = metricRawList;
	}


	@Override
	protected void filter_finalize(RootScope rootCCT, IFilterData filter) 
	{
		//------------------------------------------------------------------------------------------
		// removing the original root for caller tree and flat tree
		//------------------------------------------------------------------------------------------
		Scope root = getRootScope();
		root.getChildren().removeIf(c -> ((RootScope)c).getType() != RootScopeType.CallingContextTree);
		
		//------------------------------------------------------------------------------------------
		// filtering callers tree (bottom-up):
		// check if the callers tree has been created or not. If it's been created,
		// we need to remove it and create a new filter.
		// otherwise, we do nothing, and let the viewer to create dynamically
		//------------------------------------------------------------------------------------------
		prepareCallersView(rootCCT);
		
		//------------------------------------------------------------------------------------------
		// creating the flat tree from the filtered cct tree
		//------------------------------------------------------------------------------------------
		// While creating the flat tree, we attribute the cost for procedure scopes
		// One the tree has been created, we compute the inclusive cost for other scopes
		prepareFlatView(rootCCT);
		
		//----------------------------------------------------------------------------------------------
		// Datacentric View
		//----------------------------------------------------------------------------------------------
		prepareDatacentricView();
	}

	@Override
	protected void open_finalize() {
		postprocess(flag == ExperimentOpenFlag.TREE_ALL);		
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BaseMetric> getRawMetrics() {
		return ((List<BaseMetric>)(List<? extends BaseMetric>) metrics_raw);
	}

	@Override
	public String getPath() {
		return getExperimentFile().getAbsolutePath();
	}

	@Override
	public List<BaseMetric> getMetricList() {
		return getMetrics();
	}
}
