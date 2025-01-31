// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote;

import edu.rice.cs.hpcbase.IDatabaseIdentification;

public class RemoteDatabaseIdentification implements IDatabaseIdentification 
{
	private static final String EMPTY = "";

	private static final char SIGN_REMOTE_INSTALLATION = '*';
	private static final char SIGN_USERNAME = '@';
	private static final char SIGN_HOST = ':';
	
	private final String databasePath;
	private final String host;	
	private final String username;
	
	private String remoteInstallation;
	
	/***
	 * Create an empty remote ID
	 */
	public RemoteDatabaseIdentification() {
		databasePath = EMPTY;
		host = EMPTY;
		username = EMPTY;
		remoteInstallation = EMPTY;
	}
	
	
	/***
	 * Initialize with the existing remote database id.
	 * Example remote id format:
	 * <pre>
	 * installation_path*user@hostname:/home/user/data/hpctoolkit-database/
	 * </pre>
	 * @param databaseId
	 */
	public RemoteDatabaseIdentification(String databaseId) throws InvalidRemoteIdentficationException{		
		// partition the id into:
		//    installation * user @ hostName : database
		// installation 0 .. *
		// userName: * .. @
		// hostName: @ .. :
		// database: : ..
		
		var slash = databaseId.indexOf(SIGN_REMOTE_INSTALLATION);
		if (slash < 0)
			throw new InvalidRemoteIdentficationException(databaseId);
		
		remoteInstallation = databaseId.substring(0, slash);
		
		var atSign = databaseId.indexOf(SIGN_USERNAME, slash);
		var colon = databaseId.indexOf(SIGN_HOST, atSign);
		
		if (atSign < 0 || colon < 0) {
			throw new InvalidRemoteIdentficationException("Not a remote ID: " + databaseId);
		}
		
		username = databaseId.substring(slash+1, atSign);
		host = databaseId.substring(atSign+1, colon);
		databasePath = databaseId.substring(colon+1);
	}

	
	/***
	 * Create an instance of remote ID.
	 * 
	 * @param host
	 * @param path
	 * @param username
	 */
	public RemoteDatabaseIdentification(String host, String path, String username) {
		this.host = host;		
		this.databasePath = path;
		this.username = username;
		remoteInstallation = EMPTY;
	}
	
	/***
	 * Return the remote host.
	 * 
	 * @return {@code String} 
	 * @apiNote can be {@code null}
	 */
	public String getHost() {
		return host;
	}
	
	
	/***
	 * Return the database path.
	 * 
	 * @return {@code String}
	 * @apiNote can be {@code null}
	 */
	public String getPath() {
		return databasePath;
	}
	
	
	/***
	 * Return the user name.
	 * 
	 * @return {@code String}
	 * @apiNote can be {@code null}
	 */
	public String getUsername() {
		return username;
	}
	
	
	public void setRemoteInstallation(String remoteInstallation) {
		this.remoteInstallation = remoteInstallation;
	}
	
	
	/***
	 * Return the remote installation.
	 * 
	 * @return {@code String}
	 * @apiNote can be {@code null}
	 */
	public String getRemoteInstallation() {
		return remoteInstallation;
	}
	
	
	private String getBaseId() {
		StringBuilder sb = new StringBuilder();
		if (username != null && !username.isEmpty()) {
			sb.append(username);
			sb.append(SIGN_USERNAME);
		}
		if (host != null)
			sb.append(host.trim());
		
		if (databasePath != null) {
			sb.append(SIGN_HOST);
			sb.append(databasePath);
		}
		
		return sb.toString();
	}
	
	
	@Override
	public String id() {
		StringBuilder sb = new StringBuilder();
		if (remoteInstallation != null) {
			sb.append(remoteInstallation);
			sb.append(SIGN_REMOTE_INSTALLATION);
		}
		sb.append(getBaseId());
		
		return sb.toString();
	}		
	
	
	@Override
	public String toString() {
		return getBaseId();
	}
	
	
	@Override
	public int hashCode() {
		return databasePath.hashCode();
	}
	
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof IDatabaseIdentification other) {
			return id().equals(other.id());
		}
		return false;
	}
}