// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcremote.data;

import org.hpctoolkit.hpcclient.v1_0.HpcClient;

import edu.rice.cs.hpcremote.ISecuredConnection;
import edu.rice.cs.hpcremote.ISecuredConnection.ISessionRemoteSocket;

public interface IRemoteDatabaseConnection 
{
	HpcClient getHpcClient();
	
	ISecuredConnection getConnection();

	ISessionRemoteSocket getRemoteSocket();
}
