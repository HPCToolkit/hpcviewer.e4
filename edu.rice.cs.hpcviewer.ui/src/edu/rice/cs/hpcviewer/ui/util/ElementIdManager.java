package edu.rice.cs.hpcviewer.ui.util;

import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;

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
		return path;
	}
	
	static public String getElementId(RootScope root) {
		String elementId = getElementId(root.getExperiment()) + ELEMENT_SEPARATOR + root.getType().toString();
		return elementId;
	}

}
