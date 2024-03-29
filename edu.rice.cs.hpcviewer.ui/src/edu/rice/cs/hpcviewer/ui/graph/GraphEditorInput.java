package edu.rice.cs.hpcviewer.ui.graph;

import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.scope.Scope;


public class GraphEditorInput 
{
	static public final int MAX_TITLE_CHARS = 100; // maximum charaters for a title

	private final Scope      scope;
	private final BaseMetric metric;
	private final String     graphType;
	
	private final IThreadDataCollection threadData;
	

	/***
	 * Create a new editor input for a give scope, metric, plot type and database
	 * @param threadData
	 * @param scope
	 * @param metric
	 * @param type
	 */
	public GraphEditorInput(IThreadDataCollection threadData, 
							Scope scope, 
							BaseMetric metric, 
							String graphType) {
		
		this.threadData = threadData;
		this.graphType  = graphType;
		
		this.scope = scope;
		this.metric = metric;
	}
	
	
	public Scope getScope() {
		return scope;
	}
	
	public BaseMetric getMetric() {
		return metric;
	}
	
	public String getGraphType() {
		return graphType;
	}

	public IThreadDataCollection getThreadData() {
		return threadData;
	}
	
	public String toString() {
		
		String scopeName = scope.getName();
		if (scopeName.length() > MAX_TITLE_CHARS) {
			scopeName = scope.getName().substring(0, MAX_TITLE_CHARS) + "...";
		}
		String title = "[" + graphType + "] " + scopeName +": " + metric.getDisplayName();

		return title;
	}
}
