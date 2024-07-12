// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcremote;

public class InvalidRemoteIdentficationException extends Exception 
{
	public InvalidRemoteIdentficationException(String string) {
		super(string);
	}

	private static final long serialVersionUID = 1L;

}
