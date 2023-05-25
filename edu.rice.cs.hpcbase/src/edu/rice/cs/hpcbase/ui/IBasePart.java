package edu.rice.cs.hpcbase.ui;

import edu.rice.cs.hpcdata.experiment.BaseExperiment;

/****
 * 
 * Top interface for hpcviewer parts (views and editors)
 * 
 * Each part must implement two methods:
 * <ol>
 * <li> setInput(MPart, Object) : to set the input.</li>
 * <li> getExperiment(): to identify with which experiment database the part is belong to.</li> 
 * </ol>
 */
public interface IBasePart 
{
	BaseExperiment getExperiment();
	
	void setInput(Object input);
}
