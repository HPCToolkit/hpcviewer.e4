package edu.rice.cs.hpc.data.experiment;

import java.io.File;

import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.filter.IFilterData;
import edu.rice.cs.hpc.data.trace.TraceAttribute;
import edu.rice.cs.hpc.data.util.IUserData;

/*************************************
 * 
 * Experiment class without metrics
 *
 *************************************/
public class ExperimentWithoutMetrics extends BaseExperiment 
{
	/***** attributes of the traces ***/
	private TraceAttribute attribute;

	public void open(File fileExperiment, IUserData<String, String> userData)
			throws	Exception
	{
		super.open(fileExperiment, userData, false);
	}

	
	/******
	 * set the trace attributes (if the tracefile exist)
	 * @param _attribute
	 */
	public void setTraceAttribute(TraceAttribute _attribute) {
		this.attribute = _attribute;
	}


	/*****
	 * get the trace attributes. If the database has no traces,
	 * it return null
	 * 
	 * @return trace attributes
	 */
	public TraceAttribute getTraceAttribute() {
		return this.attribute;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.IExperiment#duplicate()
	 */
	@Override
	public ExperimentWithoutMetrics duplicate() {

		ExperimentWithoutMetrics copy = new ExperimentWithoutMetrics();
		copy.configuration 	= configuration;
		copy.databaseRepresentation = databaseRepresentation;
		
		return copy;
	}


	@Override
	protected void filter_finalize(RootScope rootMain,
			IFilterData filter) {	}


	@Override
	protected void open_finalize() {
	}

}
