//////////////////////////////////////////////////////////////////////////
//																		//
//	RootScope.java														//
//																		//
//	experiment.scope.RootScope -- root scope of an experiment			//
//	Last edited: May 18, 2001 at 6:19 pm								//
//																		//
//	(c) Copyright 2001 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.scope;


import java.io.IOException;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.BaseExperimentWithMetrics;
import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.metric.IMetricValueCollection;
import edu.rice.cs.hpc.data.experiment.metric.version2.MetricValueCollection2;
import edu.rice.cs.hpc.data.experiment.metric.version3.MetricValueCollection3;

import edu.rice.cs.hpc.data.experiment.scope.visitors.IScopeVisitor;
import edu.rice.cs.hpc.data.util.Constants;




//////////////////////////////////////////////////////////////////////////
//	CLASS ROOT-SCOPE													//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * The root scope of an HPCView experiment.
 *
 */


public class RootScope extends Scope
{
static final private String NAME = "Experiment Aggregate Metrics";

/** The name of the experiment's program. */
protected String rootScopeName;
protected RootScopeType rootScopeType;
private BaseExperiment experiment;
private String name;

private IThreadDataCollection threadData;
private MetricValueCollection3 metricSparseCollection;

//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a RootScope.
 ************************************************************************/
	
public RootScope(BaseExperiment experiment, String name, RootScopeType rst)
{
	// we assume the root scope CCT and Flat ID is 1
	super(null, null, Scope.NO_LINE_NUMBER, Scope.NO_LINE_NUMBER, 0, 0);	
	this.rootScopeName 	= name;
	this.experiment 	= experiment;
	this.rootScopeType 	= rst;
	root = this;
}

@Override
public Scope duplicate() {
    return new RootScope(experiment,  this.rootScopeName, this.rootScopeType);
}

/******
 * Retrieve (and create) the metric collection based on the version of the database.
 *  
 * @param scope : the current scope
 * @return IMetricValueCollection
 * @throws IOException
 */
public IMetricValueCollection getMetricValueCollection(Scope scope) throws IOException
{
	final int metric_size = ((BaseExperimentWithMetrics)experiment).getMetricCount();
	final int version  	  = experiment.getMajorVersion();
	
	// TODO: this is a hack
	
	if (version == Constants.EXPERIMENT_SPARSE_VERSION && rootScopeType == RootScopeType.CallingContextTree) 
	{
		if (metricSparseCollection == null) {
			metricSparseCollection = new MetricValueCollection3(this, scope);
		}
		return metricSparseCollection;
	} else {
		return new MetricValueCollection2(metric_size);		
	}
}



/****
 * set the IThreadDataCollection object to this root
 * 
 * @param threadData
 */
public void setThreadData(IThreadDataCollection threadData)
{
	this.threadData = threadData;
}


/***
 * Return the IThreadDataCollection of this root if exists.
 * 
 * @return
 */
public IThreadDataCollection getThreadData() {
	return threadData;
}

//////////////////////////////////////////////////////////////////////////
//	SCOPE DISPLAY														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the user visible name for this root scope.
 ************************************************************************/
	
public String getName()
{
	if (name == null)
		return NAME;
	else
		return name;
}

public void setName(String name)
{
	this.name = name;
}

public String getRootName()
{
	return rootScopeName;
}

public void setRootName(String name)
{
	this.rootScopeName = name;
}

public RootScopeType getType()
{
	return rootScopeType;
}


@Override
/*
 * (non-Javadoc)
 * @see edu.rice.cs.hpc.data.experiment.scope.Scope#getExperiment()
 */
public BaseExperiment getExperiment()
{
	return experiment;
}

public void setExperiment(BaseExperiment experiment)
{
	this.experiment = experiment;
}


//////////////////////////////////////////////////////////////////////////
// support for visitors													//
//////////////////////////////////////////////////////////////////////////

public void accept(IScopeVisitor visitor, ScopeVisitType vt) {
	visitor.visit(this, vt);
}

	
}

