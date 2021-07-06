package edu.rice.cs.hpcmetric.internal;

import edu.rice.cs.hpcfilter.dialog.FilterDataItem;

public class MetricFilterDataItem extends FilterDataItem implements Comparable<MetricFilterDataItem>
{
	private int id;

	public MetricFilterDataItem(int id, String label, boolean checked, boolean enabled) {
		super(label, checked, enabled);
		this.id = id;
	}

	@Override
	public int compareTo(MetricFilterDataItem o) {
		return id-o.id;
	}
}
