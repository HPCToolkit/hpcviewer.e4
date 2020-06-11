package edu.rice.cs.hpcviewer.ui.parts;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;

public interface IBaseView extends IBasePart
{
	/**
	 * tell the view its database.
	 * The view has to be smart enough whether to populate the table or not.
	 *  
	 * @param experiment database experiment
	 * @param part the object of this view part
	 */
	public void setExperiment(BaseExperiment experiment, MPart part);
		
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
