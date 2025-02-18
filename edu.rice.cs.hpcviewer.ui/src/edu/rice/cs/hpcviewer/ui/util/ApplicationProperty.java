// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcviewer.ui.util;

import java.io.File;

import org.eclipse.core.runtime.Platform;


public class ApplicationProperty 
{
	private static final String FILE_LOG_NAME = "hpcviewer.log";
	
	/***
	 * Retrieve the absolute path of the log file
	 * @return String
	 */
	public static String getFileLogLocation() {
		return Platform.getInstanceLocation().getURL().getFile() + File.separator + FILE_LOG_NAME;
	}
}
