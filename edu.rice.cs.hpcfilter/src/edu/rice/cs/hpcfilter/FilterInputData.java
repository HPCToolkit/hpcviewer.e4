// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcfilter;

import java.util.List;


public class FilterInputData<T> 
{
	private final List<FilterDataItem<T>> listItems;

	public FilterInputData(List<FilterDataItem<T>> list) {
		this.listItems = list;
	}

	public List<FilterDataItem<T>> getListItems() {
		return listItems;
	}
}
