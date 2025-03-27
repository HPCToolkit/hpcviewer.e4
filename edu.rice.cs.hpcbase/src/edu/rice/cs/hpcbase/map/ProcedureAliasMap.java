// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcbase.map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;


/**
 * 
 * class to manage procedure aliases
 *
 */
public class ProcedureAliasMap extends AliasMap<String,String> {

	static private final String FILE_NAME = "procedure.map";

	/*
	 * (non-Javadoc)
	 * @see org.hpctoolkit.db.local.util.IUserData#getFilename()
	 */
	public String getFilename() {
		
		IPath path = Platform.getLocation().makeAbsolute();
		return path.append(FILE_NAME).makeAbsolute().toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.hpctoolkit.db.local.util.IUserData#initDefault()
	 */
	public void initDefault() {
		data.put("hpcrun_special_IDLE", "... IDLE ...");
	}
}
