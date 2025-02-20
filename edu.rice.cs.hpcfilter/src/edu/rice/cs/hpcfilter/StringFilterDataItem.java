// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcfilter;

/****
 * 
 * The String data version of {@code FilterDataItem}
 *
 */
public class StringFilterDataItem extends FilterDataItem<String>
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
	public int compareTo(FilterDataItem<String> o) {
		return getLabel().compareTo(o.data);
	}

}
