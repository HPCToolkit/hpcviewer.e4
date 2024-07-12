// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcdata.merge;

import java.util.ArrayList;
import java.util.List;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;

public class ListMergedMetrics extends ArrayList<BaseMetric> 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4722486682710417753L;

	/***
	 * index offset for the future item addition
	 */
	private int offset;

	public ListMergedMetrics(int offset) {
		setOffset(offset);
	}

	public ListMergedMetrics(List<BaseMetric> metricList) {
		super(metricList);
	}

	public ListMergedMetrics(int offset, List<BaseMetric> metricList) {
		super(metricList);
		setOffset(offset);
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
}
