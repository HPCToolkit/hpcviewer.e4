package edu.rice.cs.hpcviewer.ui.internal;

import edu.rice.cs.hpc.data.experiment.Experiment;

public class ViewerDataEvent 
{	
	static public final String TOPIC_HIDE_SHOW_COLUMN = "hpcviewer/column_hide";
	
	/** Event when a new database has arrived. */
	static final public String TOPIC_HPC_NEW_DATABASE = "hpcviewer/database_add";

	/** Event when a database has to be removed from the application */
	static final public String EVENT_HPC_REMOVE_DATABASE = "hpcviewer/database_remove";

	public Experiment experiment;
	public Object 	   data;
	
	public ViewerDataEvent(Experiment experiment, Object data) {
		this.experiment = experiment;
		this.data		= data;
	}
}
