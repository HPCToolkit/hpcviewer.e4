// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.data.util;

import java.util.Iterator;
import java.util.Map.Entry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcbase.map.AliasMap;
import edu.rice.cs.hpcbase.map.ProcedureClassData;
import edu.rice.cs.hpcdata.util.Util;
import edu.rice.cs.hpcdata.util.Constants;

/***
 * 
 * Class to manage map between a procedure and its class
 * For instance, we want to class all MPI_* into mpi class, 
 * 	the get() method will then return all MPI functions into mpi
 *
 */
public class ProcedureClassMap extends AliasMap<String,ProcedureClassData> {

	public static final String CLASS_IDLE = "idle";
	private static final String FILENAME = "proc-class.map";
	private final Display display;
	
	public ProcedureClassMap(Display display) {
		this.display = display;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.common.util.ProcedureMap#getFilename()
	 */
	public String getFilename() {
		IPath path;
		if (Platform.isRunning()) {
			path = Platform.getLocation().makeAbsolute();			
			return path.append(FILENAME).makeAbsolute().toString();
		} else {
			return FILENAME;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.common.util.ProcedureMap#initDefault()
	 */
	public void initDefault() {

		if (display == null)
			return;
		
		final Color white = display.getSystemColor(SWT.COLOR_WHITE); 

		clear();
		
		put(Constants.PROC_NO_THREAD, 	CLASS_IDLE, white); // backward compatibility
		put(Constants.PROC_NO_ACTIVITY, CLASS_IDLE, white);
	}
	
	public Object[] getEntrySet() {
		checkData();
		return data.entrySet().toArray();
	}

	@Override
	public ProcedureClassData get(String key) {
		Entry<String, ProcedureClassData> entry = getEntry(key);
		if (entry != null) {
			return entry.getValue();
		}
		return null;
	}

	/***
	 * Return the pair of <glob_pattern, procedure data> of a given key.
	 * This method is useful if the caller wants to know which pattern that
	 * matches the key.
	 * 
	 * @param key
	 * @return a set of glob_pattern and procedure data if the key exists.
	 */
	public Entry<String, ProcedureClassData> getEntry(String key) {
		checkData();
		Iterator<Entry<String, ProcedureClassData>> iterator = data.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, ProcedureClassData> entry = iterator.next();
			
			// convert glob pattern into regular expression
			String glob = Util.convertGlobToRegex(entry.getKey()); 
			if (key.equals(glob) || key.matches(glob)) {
				return entry;
			}
		}
		return null;
	}
	
	public void put(String key, String val, Color image) {
		if (image != null)
			put(key,new ProcedureClassData(val,image));
	}

	public void put(String key, String val, RGB rgb) {
		put(key,new ProcedureClassData(val,rgb));
	}

	@Override
	public ProcedureClassData remove(String key) {
		return data.remove(key);
	}
	
	
	@Override
	protected boolean checkData(Entry<String, ProcedureClassData> entry) {
		// fix issue hpcviewer#371 to force to known type and then throw exception if
		// the data is corrupted
		//
		// ugly code, until we find a better solution
		try {
			String key = entry.getKey();
			ProcedureClassData val = entry.getValue();
			
			return !key.isEmpty() && val.getRGB() != null;
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass()).error("Invalid data in " + FILENAME, e);
		}
		return false;
	}
	
	
	public void refresh() {
		super.dispose();
		super.checkData();
	}
}
