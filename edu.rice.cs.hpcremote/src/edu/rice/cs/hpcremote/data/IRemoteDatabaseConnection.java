// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.data;

import org.hpctoolkit.db.client.BrokerClient;

import edu.rice.cs.hpcremote.ISecuredConnection;
import edu.rice.cs.hpcremote.ISecuredConnection.ISessionRemoteSocket;

public interface IRemoteDatabaseConnection 
{
	BrokerClient getHpcClient();
	
	ISecuredConnection getConnection();

	ISessionRemoteSocket getRemoteSocket();
}
