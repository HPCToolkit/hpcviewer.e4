// SPDX-FileCopyrightText: 2025 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.data;


public interface IServerConnectionConfig extends IServerResponse
{
	/***
	 * Unique precise host name of the server.
	 * If the login node has multiple IPs, the server will send exactly the IP it was launched.
	 * 
	 * @return {@code String}
	 */
	String getHost();
	
	/***
	 * Get the main socket to communicate such as to get the content of a directory,
	 * or to open a database, or to shut down.
	 * 
	 * @return
	 */
	String getMainSocket();
	
	
	/***
	 * Get the special communication socket.
	 * This socket is to know if the other end is still alive or not.
	 * 
	 * @return
	 */
	String getCommSocket();
}
