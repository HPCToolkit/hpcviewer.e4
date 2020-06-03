package edu.rice.cs.hpcviewer.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeColumn;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.IMetricManager;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;
import edu.rice.cs.hpcviewer.ui.metric.MetricColumnDialog;
import edu.rice.cs.hpcviewer.ui.util.FilterDataItem;

public class MetricColumnHideShowAction 
{
	private boolean affectOtherViews;

	
	public MetricColumnHideShowAction(boolean affectOtherViews) {
		this.affectOtherViews = affectOtherViews;
	}
	
	   /**
     * Show column properties (hidden, visible ...)
     */
    public void showColumnsProperties(ScopeTreeViewer treeViewer, IMetricManager metricMgr) {

    	TreeColumn []columns     = treeViewer.getTree().getColumns();    	
		List<BaseMetric> metrics = metricMgr.getVisibleMetrics();
		if (metrics == null)
			return;
		
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
		Shell shell = treeViewer.getTree().getShell();
		
    	MetricColumnDialog dialog = new MetricColumnDialog(shell, arrayOfItems);
    	dialog.enableAllViewOption(affectOtherViews);
    	if (dialog.open() == Dialog.OK) {
    		boolean isAppliedToAllViews = dialog.isAppliedToAllViews();
    		arrayOfItems = dialog.getResult();
    		
    		boolean []checked = new boolean[arrayOfItems.size()];
    		int i = 0;
    		for (FilterDataItem item : arrayOfItems) {
				checked[i] = item.checked && item.enabled;
				i++;
    		}
    		
    		if (isAppliedToAllViews) {
    			
    			// send message to all registered views, that there is a change of column properties
    			// we don't verify if there's a change or not. Let the view decides what they want to do 
    			
    			
    		} else {
    			treeViewer.setColumnsStatus(checked);
    		}
    	}
    }
    
   
}