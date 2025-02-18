// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.data;


public interface IServerResponse 
{
	/**
	 * Types of server responses:
	 * <ul>
	 *   <li>SUCCESS : everything works fine
	 *   <li>ERROR : something doesn't work right. Need to abandon the process.
	 *   <li>INVALID: something strange happens, perhaps empty string. Need to continue the process carefully.
	 * </ul>
	 */
	enum ServerResponseType {SUCCESS, ERROR, INVALID}

	ServerResponseType getResponseType();
	
}
