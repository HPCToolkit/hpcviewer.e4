package edu.rice.cs.hpcviewer.ui.util;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;

/******************************************************************
 * 
 * Class to manage and standardize the element ID of all parts
 *
 ******************************************************************/
public class ElementIdManager 
{
	static public final String ELEMENT_SEPARATOR = ":";
	
	static public String getElementId(BaseExperiment experiment) {
		// has to set the element Id before populating the view
		String path = experiment.getXMLExperimentFile().getAbsolutePath();
		int pathId  = path.hashCode();
		String elementId = String.valueOf(pathId);
		
		return elementId;
	}
	
	static public String getElementId(RootScope root) {
		String elementId = getElementId(root.getExperiment()) + ELEMENT_SEPARATOR + root.getName();
		
		return elementId;
	}

}
