// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcmetric;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;

import edu.rice.cs.hpcbase.BaseConstants.ViewType;
import edu.rice.cs.hpcbase.IEditorViewerInput;
import edu.rice.cs.hpcbase.ui.IUpperPart;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.FilterInputData;
import edu.rice.cs.hpcmetric.internal.MetricFilterDataItem;

public class MetricFilterInput extends FilterInputData<BaseMetric> implements IEditorViewerInput
{
	private final IFilterable view;
	private final IEventBroker eventBroker;

	/****
	 * Constructor for the metric filter input
	 * 
	 *  @param view The table of {@code IFilterable} to be filtered
	 *  @param eventBroker event broadcaster when the filter is executed
	 */
	public MetricFilterInput(IFilterable view, IEventBroker eventBroker) {
		super(view.getFilterDataItems());
		
		this.view = view;
		this.eventBroker = eventBroker;
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
		return view.getMetricManager();
	}


	public RootScope getRoot() {
		return view.getRoot();
	}


	public boolean isAffectAll() {
		return view.getViewType() == ViewType.COLLECTIVE;
	}


	@Override
	public String getId() {
		return view.getMetricManager().getID();
	}


	@Override
	public String getShortName() {
		return "Metric view";
	}


	@Override
	public String getLongName() {
		return getShortName() + ": " + getId();
	}


	@Override
	public IUpperPart createViewer(Composite parent) {

		return new MetricView((CTabFolder) parent, SWT.NONE, eventBroker);
	}


	@Override
	public boolean needToTrackActivatedView() {
		return true;
	}

	
}
