// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcbase;

import edu.rice.cs.hpcdata.util.Constants;

public interface BaseConstants 
{
	enum ViewType {COLLECTIVE, INDIVIDUAL}


	/** Event when a database has to be removed from the application */
	String TOPIC_HPC_REMOVE_DATABASE = "hpcviewer/database_remove";
	
    
	/**The darkest color for black over depth text (switch to white if the sum of the 
	 * R, G, and B components is less than this number).*/
	int DARKEST_COLOR_FOR_BLACK_TEXT = 384;
	
	
	int TRACE_RECORD_SIZE = Constants.SIZEOF_LONG // time stamp
					        + Constants.SIZEOF_INT; // call path id

}
