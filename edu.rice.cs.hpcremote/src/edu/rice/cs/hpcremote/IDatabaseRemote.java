// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote;

import org.hpctoolkit.db.client.BrokerClient;

import edu.rice.cs.hpcbase.IDatabase;


public interface IDatabaseRemote extends IDatabase
{
	BrokerClient getClient();
}
