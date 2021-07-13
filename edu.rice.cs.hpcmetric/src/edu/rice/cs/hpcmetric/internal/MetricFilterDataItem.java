package edu.rice.cs.hpcmetric.internal;

import org.eclipse.swt.widgets.TreeColumn;

import edu.rice.cs.hpcfilter.dialog.FilterDataItem;

public class MetricFilterDataItem extends FilterDataItem implements Comparable<MetricFilterDataItem>
{
	private final int id;
	private TreeColumn column;

	public MetricFilterDataItem(int id, TreeColumn column, String label, boolean checked, boolean enabled) {
		super(label, checked, enabled);
		this.id = id;
		this.column = column;
	}

	@Override
	public int compareTo(MetricFilterDataItem o) {
		return id-o.id;
	}
	
	
	public void setColumn(TreeColumn column) {
		this.column = column;
	}
	
	
	public TreeColumn getColumn() {
		return column;
	}
	
	
	@Override
	public String toString() {
		return (super.toString() + ", " + id + ", " + column.getText());
	}
}
