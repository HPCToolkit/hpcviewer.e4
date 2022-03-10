//////////////////////////////////////////////////////////////////////////
//																		//
//	RootScope.java														//
//																		//
//	experiment.scope.RootScope -- root scope of an experiment			//
//	Last edited: May 18, 2001 at 6:19 pm								//
//																		//
//	(c) Copyright 2002-2022 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpcdata.experiment.scope;


import java.io.IOException;

import edu.rice.cs.hpcdata.db.MetricValueCollectionWithStorage;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.metric.IMetricValueCollection;
import edu.rice.cs.hpcdata.experiment.scope.visitors.IScopeVisitor;




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
public static final String DEFAULT_SCOPE_NAME = "Experiment Aggregate Metrics";

public static final int DEFAULT_CCT_ID  = 0;
public static final int DEFAULT_FLAT_ID = 0;

/** The name of the experiment's program. */
protected String rootScopeName;
protected RootScopeType rootScopeType;
private IExperiment experiment;
private String name;
private IMetricValueCollection mvc;



//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////


/*************************************************************************
 *	Creates a RootScope.
 ************************************************************************/

public RootScope(IExperiment experiment, String name, RootScopeType rst)
{
	this(experiment, name, rst, DEFAULT_CCT_ID, DEFAULT_FLAT_ID);
}

public RootScope(IExperiment experiment, String name, RootScopeType rst, int cctId, int flatId)
{
	super(null, null, Scope.NO_LINE_NUMBER, Scope.NO_LINE_NUMBER, cctId, flatId);	
	this.rootScopeName 	= name;
	this.experiment 	= experiment;
	this.rootScopeType 	= rst;
	root = this;
}


@Override
public Scope duplicate() {
	int cctId  = getCCTIndex();
	int flatId = getFlatIndex();
	
    return new RootScope(experiment,  this.rootScopeName, this.rootScopeType, cctId, flatId);
}



/****
 * Set the default metric value collection object. 
 * This object is used to generate IMetricValueCollection instance inside a scope.
 * 
 * For sparse and dense databases have different implementation of IMetricValueCollection.
 * 
 * @param mvc
 */
public void setMetricValueCollection(IMetricValueCollection mvc) {
	this.mvc = mvc;
}


/******
 * Retrieve (and create) the metric collection based on the version of the database.
 *  
 * @return IMetricValueCollection
 * @throws IOException
 */
public IMetricValueCollection getMetricValueCollection() throws IOException
{
	if (mvc != null) {
		return mvc.duplicate();
	}
	return new MetricValueCollectionWithStorage();		
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
		return DEFAULT_SCOPE_NAME;
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
public IExperiment getExperiment()
{
	return experiment;
}

public void setExperiment(IExperiment experiment)
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

