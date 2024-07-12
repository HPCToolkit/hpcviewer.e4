// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcremote.data;

/**************************************
 * 
 * Interface for listening a thread
 * 
 * - the master (listener) has to implement the interface and
 * 	 do an action based on notify method
 * 
 * - The child thread needs to call notify() once an error occurs 
 *
 */
public interface IThreadListener {
	
	/***
	 * Notify the listener that something has happened
	 * @param msg
	 */
	public void notify(String msg);
	
}
