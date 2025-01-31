// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote;

public class DefaultConnection implements IConnection 
{
	private final RemoteDatabaseIdentification databaseId;
	
	public DefaultConnection(RemoteDatabaseIdentification databaseId) {
		this.databaseId = databaseId;
	}
	
	@Override
	public String getHost() {
		return databaseId.getHost();
	}

	@Override
	public String getUsername() {
		return databaseId.getUsername();
	}

	@Override
	public String getInstallationDirectory() {
		return databaseId.getRemoteInstallation();
	}

	@Override
	public String getProxyAgent() {
		return null;
	}

}
