// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

/**
 * 
 */
package edu.rice.cs.hpcviewer.ui.util;

import java.io.File;


/**
 * Class providing auxiliary utilities methods.
 * Remark: it is useless to instantiate this class since all its methods are static !
 *
 */
public class Utilities 
{		    
    
    /****
     * get the default workspace directory. 
     * A workspace directory is the location where Eclipse will store caches (plugin, libraries),
     * preferences, logs, file locks, etc.
     * We may need to store all user setting there too.
     * 
     * @return {@code String}
     */
    public static String getWorkspaceDirectory() {
		
		final String arch = System.getProperty("os.arch");

		final String subDir = ".hpctoolkit" + File.separator + 
							  "hpcviewer"   + File.separator +
							  arch;
		
		return System.getProperty("user.home") + File.separator + subDir;
    }
}
