// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcmetric;

import java.util.List;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcbase.BaseConstants.ViewType;

public interface IFilterable 
{
	/***
	 * Return the list of items to be filtered (if to be viewed) 
	 * or have been filtered (if have been viewed)
	 * 
	 * @return list of metrics to be filtered or have been filtered
	 * 
	 * @see FilterDataItem
	 */
	List<FilterDataItem<BaseMetric>> getFilterDataItems();
	
	/***
	 * Return the type of the view
	 * 
	 * @return ViewType
	 * 
	 * @see ViewType
	 */
	ViewType getViewType();

	
	/**
	 * Return the current metric manager
	 * 
	 * @return a metric manager or experiment
	 * 
	 * @see IMetricManager
	 */
	IMetricManager getMetricManager();
	
	
	/**
	 * Return the root scope of this view
	 * 
	 * @return a root scope
	 * 
	 * @see RootScope
	 */
	RootScope getRoot();
}
