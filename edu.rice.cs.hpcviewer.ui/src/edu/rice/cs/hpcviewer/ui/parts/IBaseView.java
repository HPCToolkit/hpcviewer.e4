package edu.rice.cs.hpcviewer.ui.parts;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;

public interface IBaseView 
{
	public void setExperiment(BaseExperiment experiment);
	
	/**
	 * View types, see Experiment.TITLE_* 
	 * 
	 */
	public String getViewType();
	
	public String getID();
}
