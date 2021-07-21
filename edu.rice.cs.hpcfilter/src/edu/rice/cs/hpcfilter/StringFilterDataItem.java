package edu.rice.cs.hpcfilter;

/****
 * 
 * The String data version of {@code FilterDataItem}
 *
 */
public class StringFilterDataItem extends FilterDataItem 
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

}
