// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcfilter;

public interface IFilterChangeListener 
{
	/****
	 * List of callbacks when there is a change  in the filter items
	 * If users changed the several items, the param is a list of item.
	 * Otherwise the param is an object item
	 * 
	 * @param data can be either {@code List<FilterDataItem>} or {@code FilterDataItem}
	 */
	void changeEvent(Object data);	
}
