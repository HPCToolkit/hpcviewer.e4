package edu.rice.cs.hpcbase;

import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;

/******************************************************************
 * 
 * Class to manage and standardize the element ID of all parts
 *
 ******************************************************************/
public class ElementIdManager 
{
	public static final String ELEMENT_SEPARATOR = ":";
	
	public static String getElementId(IExperiment iExperiment) {
		// has to set the element Id before populating the view
		return iExperiment.getPath();
	}
	
	public static String getElementId(RootScope root) {
		return getElementId(root.getExperiment()) + ELEMENT_SEPARATOR + root.getType().toString();
	}

}
