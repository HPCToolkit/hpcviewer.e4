package edu.rice.cs.hpcviewer.ui.parts;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;

public interface IBaseView extends IBasePart
{
		
	/**
	 * View types, see Experiment.TITLE_* 
	 * 
	 */
	public String getViewType();
	
	/***
	 * unique element ID of the view
	 * @return
	 */
	public String getID();
}
