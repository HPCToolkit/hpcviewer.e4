// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcfilter.service;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcbase.map.AliasMap;
import org.hpctoolkit.db.local.filter.FilterAttribute;
import org.hpctoolkit.db.local.filter.IFilterData;
import org.hpctoolkit.db.local.util.*;

/******************************************************************
 * 
 * Map to filter a scope either exclusively on inclusively
 * @see FilterAttribute
 *
 ******************************************************************/
public class FilterMap extends AliasMap<String, FilterAttribute> 
implements IFilterData
{
	/***
	 * Topic event constant to mark a new filter has been "updated" and
	 * the existing root has to be filtered, and the table has to refreshed.
	 */
	public static final String FILTER_REFRESH_PROVIDER = "hpcfilter/update";
	
	private static final String FILE_NAME = "filter.map";
	
	public FilterMap() {
		checkData();
	}
	
	/****
	 * Factory for generating a filter map
	 * @return FilterMap : a filter map
	 */
	public static FilterMap getInstance()
	{
		return new FilterMap();
	}
	
	public int size() 
	{
		if (data != null) {
			return data.size();
		}
		return 0;
	}
	
	/***
	 * retrieve the iterator of the hash map
	 * @return
	 */
	public Iterator<Entry<String, FilterAttribute>> iterator() {
		checkData();
		return data.entrySet().iterator();
	}
	
	/****
	 * Check if two FilterMap have the same key and values
	 * @param other
	 * @return
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof FilterMap) {
			FilterMap otherMap = (FilterMap) other;
			if (data.size() != otherMap.data.size())
				return false;
			
			return data.equals(otherMap.data);
		}
		return false;
	}
	
	
	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}
	
	@Override
	public String getFilename() {
		
		IPath path = null;
		try {
			path = Platform.getLocation().makeAbsolute();
		} catch (AssertionFailedException e) {
			String home = System.getProperty("user.dir");
			path = new Path(home);
		}
		return path.append(FILE_NAME).makeAbsolute().toString();
	}

	@Override
	public void initDefault() {
		// unused
	}
	
	/******
	 * retrieve a list of filters
	 * 
	 * @return
	 */
	public Object[] getEntrySet() {
		checkData();
		return data.entrySet().toArray();
	}
	

	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.filter.IFilterData#select(java.lang.String)
	 */
	public boolean select(String element)
	{
		return getFilterAttribute(element) != null;
	}
	
	/*****
	 * Check whether a string can be filtered or not.
	 * If the string match a filter, returns the filter attribute
	 * Otherwise returns null;
	 * 
	 * <p>See {@link org.hpctoolkit.db.local.filter.FilterAttribute}
	 * </p>
	 * 
	 * @param element
	 * @return {@link FilterAttribute} 
	 * if the element matches to a filter pattern. Return null otherwise.
	 */
	@Override
	public FilterAttribute getFilterAttribute(String element) 
	{
		Object []entries = getEntrySet();
		
		// --------------------------------------------------------------------------------
		// this is a bad bad bad practice.
		// the complexity is O(NM) where N is the number of nodes and M is the number of patterns
		// we know this is not quick, but assuming M is very small, the order should be linear
		// --------------------------------------------------------------------------------
		for (Object entry : entries)
		{
			@SuppressWarnings("unchecked")
			Entry<String, FilterAttribute> pattern = (Entry<String, FilterAttribute>) entry;
			FilterAttribute toFilter = pattern.getValue();
			
			if (toFilter.enable)
			{
				// convert glob into regular expression
				final String key = Util.convertGlobToRegex(pattern.getKey());
				if (element.matches(key)) {
					return toFilter;
				}
			}
		}
		return null;
	}
	
	/*****
	 * rename the filter
	 * 
	 * @param oldKey : old filter
	 * @param newKey : new filter
	 * 
	 * @return true if the update successful, false otherwise
	 */
	public boolean update(String oldKey, String newKey)
	{
		FilterAttribute val = get(oldKey);
		return update(oldKey, newKey, val);
	}

	public boolean update(String oldKey, String newKey, FilterAttribute attribute)
	{
		FilterAttribute val = get(oldKey);
		if (val != null)
		{
			remove(oldKey);
			put(newKey, attribute);
			return true;
		}
		return false;
	}
	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.filter.IFilterData#isFilterEnabled()
	 */
	public boolean isFilterEnabled() 
	{
		boolean enabled = false;
		if (data != null && !data.isEmpty()) {
			Collection<FilterAttribute> coll = data.values();
			Iterator<FilterAttribute> iterator = coll.iterator();
			
			// iterate through the list if there is at least one pattern enabled
			while(iterator.hasNext() && !enabled) {
				FilterAttribute att = iterator.next();
				enabled = att.enable;
			}				
		}
		
		return enabled;
	}
	
	@Override
	protected boolean checkData(Entry<String, FilterAttribute> entry) {
		try {
			// issue #371 force to cast with the known type to make sure each data is correct
			var key = entry.getKey();
			var val = entry.getValue();
			return !key.isEmpty() && val.getFilterType() != null;
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass()).error("Invalid filter map file", e);
			return false;
		}
	}
}
