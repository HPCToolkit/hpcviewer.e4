package edu.rice.cs.hpcfilter;

/****
 * 
 * The String data version of {@code FilterDataItem}
 *
 */
public class StringFilterDataItem extends FilterDataItem implements Comparable<StringFilterDataItem>
{
	public StringFilterDataItem(String data, boolean checked, boolean enabled) {
		super(data, checked, enabled);
	}

	@Override
	public void setLabel(String name) {
		this.data = name;
	}

	@Override
	public String getLabel() {
		return (String) data;
	}


	@Override
	public int compareTo(StringFilterDataItem o) {
		return getLabel().compareTo(o.getLabel());
	}

}
