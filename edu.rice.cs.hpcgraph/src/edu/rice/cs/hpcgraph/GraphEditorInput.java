package edu.rice.cs.hpcgraph;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpcbase.IEditorViewerInput;
import edu.rice.cs.hpcbase.ui.IProfilePart;
import edu.rice.cs.hpcbase.ui.IUpperPart;
import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcgraph.histogram.GraphHistoViewer;
import edu.rice.cs.hpcgraph.plot.GraphPlotRegularViewer;
import edu.rice.cs.hpcgraph.plot.GraphPlotSortViewer;


public class GraphEditorInput implements IEditorViewerInput
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
	
	
	@Override
	public String toString() {
		return getLongName();
	}


	@Override
	public String getId() {
		return getLongName();
	}


	@Override
	public String getShortName() {		
		String scopeName = scope.getName();
		if (scopeName.length() > MAX_TITLE_CHARS) {
			scopeName = scope.getName().substring(0, MAX_TITLE_CHARS) + "...";
		}
		return getGenericName(scopeName);
	}


	@Override
	public String getLongName() {
		return getGenericName(scope.getName());
	}


	@Override
	public IUpperPart createViewer(Composite parent) {
		IUpperPart viewer = null;
		
		if (graphType == GraphPlotRegularViewer.LABEL) {
			viewer = new GraphPlotRegularViewer((CTabFolder) parent, SWT.NONE);
		} else if (graphType == GraphPlotSortViewer.LABEL) {
			viewer = new GraphPlotSortViewer((CTabFolder) parent, SWT.NONE);
		} else if (graphType == GraphHistoViewer.LABEL) {
			viewer = new GraphHistoViewer((CTabFolder) parent, SWT.NONE);
		}
		return viewer;
	}


	@Override
	public boolean needToTrackActivatedView() {
		return false;
	}
	
	
	private String getGenericName(String scopeName) {
		return "[" + graphType + "] " + scopeName +": " + metric.getDisplayName();
	}
}
