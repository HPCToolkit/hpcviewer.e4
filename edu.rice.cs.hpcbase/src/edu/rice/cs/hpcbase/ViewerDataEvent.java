package edu.rice.cs.hpcbase;

import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;

/***************************************************************
 * 
 * Class to manage events.
 * 
 * Any parts of UI can share data based on Experiment. 
 * Each part is required to check if the experiment in this data is 
 *  exactly the same as their experiment or not. If not, they can skip the event. 
 *
 ***************************************************************/
public class ViewerDataEvent 
{	
	/** Event when metric columns have to be hidden or shown. See the data field. o*/
	public static final String TOPIC_HIDE_SHOW_COLUMN     = "hpcviewer/column_hide";
	
	/** Event when a new database has arrived. */
	public static final String TOPIC_HPC_DATABASE_NEW     = "hpcviewer/database_add";

	/** Event when a database (cct) has been changed such as filtered or name updates */
	public static final String TOPIC_FILTER_PRE_PROCESSING = "hpcviewer/filter_pre";

	/** Event when a database (cct) has been changed such as filtered or name updates */
	public static final String TOPIC_FILTER_POST_PROCESSING = "hpcviewer/filter_post";

	/** Event when a database has to be removed from the application */
	public static final String TOPIC_HPC_ADD_NEW_METRIC   = "hpcviewer/metric_add";

	/** Event when a metric has been changed or modified by the user */
	public static final String TOPIC_HPC_METRIC_UPDATE    = "hpcviewer/metric_update";
	
	public IMetricManager metricManager;
	public Object 	  data;
	
	
	/***
	 * Constructor to create a data event
	 * 
	 * @param metricManager IMetricManager object to control the metrics
	 * @param data the data to be shared among different parts
	 */
	public ViewerDataEvent(IMetricManager metricManager, Object data) {
		this.metricManager = metricManager;
		this.data		= data;
	}
	
	
	public String toString() {
		return metricManager.toString() + ", data: " + data;
	}
}
