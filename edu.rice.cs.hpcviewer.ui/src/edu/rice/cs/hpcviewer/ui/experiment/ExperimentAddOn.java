package edu.rice.cs.hpcviewer.ui.experiment;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;

@Creatable
@Singleton
public class ExperimentAddOn 
{
	static final public String EVENT_HPC_NEW_DATABASE = "hpcviewer/database";

	private ConcurrentLinkedQueue<BaseExperiment> queueExperiment;
	
	public ExperimentAddOn() {
		queueExperiment = new ConcurrentLinkedQueue<>();
	}
	
	public void addDatabase(BaseExperiment experiment) {
		queueExperiment.add(experiment);
	}
	
	public int getNumDatabase() {
		return queueExperiment.size();
	}
	
	public boolean isEmpty() {
		return queueExperiment.isEmpty();
	}
	
	public BaseExperiment getLast() {
		return queueExperiment.element();
	}
}
