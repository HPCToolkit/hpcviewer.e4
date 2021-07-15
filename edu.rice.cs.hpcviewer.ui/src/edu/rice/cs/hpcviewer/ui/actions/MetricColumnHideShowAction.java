package edu.rice.cs.hpcviewer.ui.actions;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcmetric.MetricFilterInput;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;

public class MetricColumnHideShowAction 
{	
	private boolean 	     affectOtherViews;
	private final IMetricManager metricMgr;
	public MetricColumnHideShowAction(IEventBroker eventBroker, IMetricManager metricMgr, boolean affectOtherViews) {
		this.affectOtherViews = affectOtherViews;
		this.metricMgr		  = metricMgr;
	}
	
	
	/**
     * Show column properties (hidden, visible ...)
     */
    public void showColumnsProperties(final ProfilePart profilePart, ScopeTreeViewer treeViewer, DatabaseCollection databaseCollection) {
    	
    	if (metricMgr == null)
    		return;
    	
		List<BaseMetric> metrics = metricMgr.getVisibleMetrics();
		if (metrics == null)
			return;
		
    	treeViewer.getTree().getColumns(); 

		MetricFilterInput input = new MetricFilterInput(treeViewer.getRootScope(), metricMgr, treeViewer, affectOtherViews);

		profilePart.addEditor(input);
    }
    
   
}
