// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.data;

import java.io.IOException;

import org.hpctoolkit.hpcclient.v1_0.DirectoryContentsNotAvailableException;
import org.hpctoolkit.hpcclient.v1_0.RemoteDirectory;

public interface IRemoteDirectoryBrowser 
{
	RemoteDirectory getContentRemoteDirectory( String directory) 
			throws IOException, InterruptedException, DirectoryContentsNotAvailableException;
	
	/**
	 * Get the unique IP address of the remote host
	 * @return
	 */
	String getRemoteHost();


	/****
	 * Get the human readable of the remote host
	 * 
	 * @return {@code String} The name of the remote host.
	 * 			If not connection, it returns empty string or default host name.
	 */
	String getRemoteHostname();
}
