package edu.rice.cs.hpcgraph;

import edu.rice.cs.hpcbase.ui.IProfilePart;
import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.scope.Scope;


public class GraphEditorInput 
{
	public static final int MAX_TITLE_CHARS = 100; // maximum charaters for a title

	private final IProfilePart profilePart;
	
	private final Scope      scope;
	private final BaseMetric metric;
	private final String     graphType;
	
	private IThreadDataCollection threadData;
	

	/***
	 * Create a new editor input for a give scope, metric, plot type and database
	 * @param threadData
	 * @param scope
	 * @param metric
	 * @param type
	 */
	public GraphEditorInput(IProfilePart profilePart, 
						    IThreadDataCollection threadData, 
							Scope scope, 
							BaseMetric metric, 
							String graphType) {
		
		this.profilePart = profilePart;
		
		this.threadData = threadData;
		this.graphType  = graphType;
		
		this.scope = scope;
		this.metric = metric;
	}
	
	
	/***
	 * Free attributes to allow GC to reclaim memories
	 */
	public void dispose() {
		// fix issue memory leaks:
		// we need to set this threadData to null
		threadData = null;
	}
	
	/**
	 * @return the profilePart
	 */
	public IProfilePart getProfilePart() {
		return profilePart;
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
