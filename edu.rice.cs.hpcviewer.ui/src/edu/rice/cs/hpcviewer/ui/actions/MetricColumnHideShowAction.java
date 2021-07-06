package edu.rice.cs.hpcviewer.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.widgets.TreeColumn;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcfilter.dialog.FilterDataItem;
import edu.rice.cs.hpcmetric.MetricFilterInput;
import edu.rice.cs.hpcviewer.ui.ProfilePart;
import edu.rice.cs.hpcviewer.ui.addon.DatabaseCollection;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;

public class MetricColumnHideShowAction 
{	
	private boolean 	     affectOtherViews;
	final private IEventBroker 	 eventBroker;
	final private IMetricManager metricMgr;
	
	public MetricColumnHideShowAction(IEventBroker eventBroker, IMetricManager metricMgr, boolean affectOtherViews) {
		this.affectOtherViews = affectOtherViews;
		this.eventBroker      = eventBroker;
		this.metricMgr		  = metricMgr;
	}
	
	
	/**
	 * Set the flag to specify if we want the change of status affect other views or not.
	 * 
	 * @param enabled true if the change affects other views. false if the change just affect this view.
	 */
	public void setAffectOtherViews(boolean enabled) {
		this.affectOtherViews = enabled;
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
		
    	TreeColumn []columns              = treeViewer.getTree().getColumns();    
		List<FilterDataItem> arrayOfItems = new ArrayList<FilterDataItem>(metrics.size());
		
		for(BaseMetric metric: metrics) {
			
			FilterDataItem item = new FilterDataItem(metric.getDisplayName(), false, false);
			
			// looking for associated metric in the column
			// a metric may not exit in table viewer because
			// it has no metric value (empty metric)
			
			for(TreeColumn column: columns) {
				Object data = column.getData();
				
				if (data != null) {
					BaseMetric m = (BaseMetric) data;
					if (m.equalIndex(metric)) {
						item.enabled = true;
						item.checked = column.getWidth() > 1;
						item.setData(column);
						
						break;
					}
				}
			}
			arrayOfItems.add(item);
		}

		MetricFilterInput input = new MetricFilterInput();
		input.metricManager = this.metricMgr;
		input.listItems = arrayOfItems;
		input.affectAll = this.affectOtherViews;
		input.root = treeViewer.getRootScope();

		profilePart.addEditor(input);
    }
    
   
}
