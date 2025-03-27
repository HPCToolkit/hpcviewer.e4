// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.data;

import java.io.IOException;

import org.eclipse.collections.impl.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.IntObjectMap;

import org.hpctoolkit.db.local.experiment.metric.BaseMetric;
import org.hpctoolkit.db.local.experiment.metric.IMetricValueCollection;
import org.hpctoolkit.db.local.experiment.metric.MetricValue;
import org.hpctoolkit.db.local.experiment.scope.Scope;

public class EmptyMetricValueCollection implements IMetricValueCollection
{
	private static final EmptyMetricValueCollection instance = new EmptyMetricValueCollection();

	public static EmptyMetricValueCollection getInstance() {
		return instance;
	}

	@Override
	public void appendMetrics(IMetricValueCollection mvCollection, int offset) {
		// nothing to do
	}

	@Override
	public MetricValue getValue(Scope scope, int index) {
		return MetricValue.NONE;
	}

	@Override
	public MetricValue getValue(Scope scope, BaseMetric metric) {
		return MetricValue.NONE;
	}

	@Override
	public void setValue(int index, MetricValue value) {
		// nothing to do
	}

	@Override
	public boolean hasMetrics(Scope scope) {
		return false;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public void dispose() {
		// nothing to do
	}

	@Override
	public IntObjectMap<MetricValue> getValues() {
		return IntObjectMaps.immutable.empty();
	}

	@Override
	public IMetricValueCollection duplicate() throws IOException {
		return this;
	}

}
