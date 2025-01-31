// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote;

public class InvalidRemoteIdentficationException extends Exception 
{
	public InvalidRemoteIdentficationException(String string) {
		super(string);
	}

	private static final long serialVersionUID = 1L;

}
