package edu.rice.cs.hpcfilter;

/*******
 * 
 * data structure to display metric column status:
 * <ul>
 * <li>label: the name of the metric (or column title)
 * <li>checked: if the column is showed (true) or not (false)
 * <li>enabled: if the column status can be changed (true) or not (false)
 * </ul>
 */
public abstract class FilterDataItem<T> implements Comparable<FilterDataItem<T>>
{
	public boolean checked = false;
	public boolean enabled = true;
	
	/** data associated with this item.
	 *  it shouldn't be modified by any class except the owner (caller).
	 * **/
	public T  data    = null;

	
	public FilterDataItem(T data, boolean checked, boolean enabled) {
		this.data    = data;
		this.checked = checked;
		this.enabled = enabled;
	}
	
	
	public Object getData() {
		return data;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	
	@Override
	public String toString() {
		return getLabel() + ": " + checked + ", " + enabled;
	}
	
	
	public abstract void setLabel(String name);
	public abstract String getLabel();

}
