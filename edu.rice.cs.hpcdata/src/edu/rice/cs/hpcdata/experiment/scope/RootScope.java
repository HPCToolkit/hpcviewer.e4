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




package edu.rice.cs.hpcdata.experiment.scope;


import java.io.IOException;

import edu.rice.cs.hpcdata.db.MetricValueCollectionWithStorage;
import edu.rice.cs.hpcdata.db.version4.DataSummary;
import edu.rice.cs.hpcdata.db.version4.MetricValueCollection3;
import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.metric.IMetricValueCollection;
import edu.rice.cs.hpcdata.experiment.scope.visitors.IScopeVisitor;
import edu.rice.cs.hpcdata.util.Constants;




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

static final public int DEFAULT_CCT_ID  = 0;
static final public int DEFAULT_FLAT_ID = 0;

/** The name of the experiment's program. */
protected String rootScopeName;
protected RootScopeType rootScopeType;
private BaseExperiment experiment;
private String name;

//private IThreadDataCollection threadData;
//private DataSummary dataSummary;

//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a RootScope.
 ************************************************************************/

public RootScope(BaseExperiment experiment, String name, RootScopeType rst)
{
	this(experiment, name, rst, DEFAULT_CCT_ID, DEFAULT_FLAT_ID);
}

public RootScope(BaseExperiment experiment, String name, RootScopeType rst, int cctId, int flatId)
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



/******
 * Retrieve (and create) the metric collection based on the version of the database.
 *  
 * @return IMetricValueCollection
 * @throws IOException
 */
public IMetricValueCollection getMetricValueCollection() throws IOException
{
	final int version  	  = experiment.getMajorVersion();
	
	// TODO: this is a hack
	
	if (version == Constants.EXPERIMENT_SPARSE_VERSION) 
	{
		if (rootScopeType == RootScopeType.CallingContextTree) {
			DataSummary data = experiment.getDataSummary();
			return new MetricValueCollection3(data);
			
		} else if (rootScopeType == RootScopeType.CallerTree || 
				   rootScopeType == RootScopeType.Flat) {

			return new MetricValueCollectionWithStorage();
		}
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

