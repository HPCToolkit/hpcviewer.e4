// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcremote;

import org.hpctoolkit.hpcclient.v1_0.HpcClient;

import edu.rice.cs.hpcbase.IDatabase;

public interface IDatabaseRemote extends IDatabase
{
	HpcClient getClient();
}