package edu.rice.cs.hpcmetric;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeColumn;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.FilterInputData;
import edu.rice.cs.hpcmetric.internal.MetricFilterDataItem;

public class MetricFilterInput extends FilterInputData<BaseMetric>
{
	private final RootScope root;
	private final boolean affectAll;
	private final IMetricManager metricManager;
	private final IFilterable view;

	/****
	 * Constructor for the metric filter input
	 * 
	 * @param root The root scope used to display the metric values of the root. It can be any root, but the preference is the cct root. 
	 * @param metricManager Current metric manager. Usually it's the experiment class
	 * @param listMetrics List of metric status
	 * @param affectAll boolean true if the change affects all other views within the experiment database
	 */
	public MetricFilterInput(RootScope root, IMetricManager metricManager, IFilterable view, boolean affectAll) {
		super(view.getFilterDataItems());
		this.root = root;
		this.view = view;
		this.metricManager = metricManager;
		this.affectAll = affectAll;
	}
 	
	
	/****
	 * Constructor for unit test only
	 * 
	 * @param root
	 * @param metricManager
	 * @param treeViewer
	 * @param affectAll
	 */
	public MetricFilterInput(RootScope root, IMetricManager metricManager, TreeViewer treeViewer, boolean affectAll) {	
		super(createFilterList(metricManager.getVisibleMetrics(), treeViewer));
		this.root = root;
		this.view = null;
		this.metricManager = metricManager;
		this.affectAll = affectAll;
	}
	
	public IFilterable getView() {
		return view;
	}
	
	public static List<FilterDataItem<BaseMetric>> createFilterList(List<BaseMetric> metrics, TreeViewer treeViewer) {
		List<FilterDataItem<BaseMetric>> listItems = new ArrayList<>(metrics.size());
		TreeColumn []columns = treeViewer.getTree().getColumns();
		
		for(BaseMetric metric: metrics) {
			
			MetricFilterDataItem item = new MetricFilterDataItem(metric, false, false);
			
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
						break;
					}
				}
			}
			listItems.add(item);
		}
		return listItems;
	}


	public IMetricManager getMetricManager() {
		return metricManager;
	}


	public RootScope getRoot() {
		return root;
	}


	public boolean isAffectAll() {
		return affectAll;
	}

	
}
