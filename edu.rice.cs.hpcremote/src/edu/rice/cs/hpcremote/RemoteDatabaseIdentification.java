package edu.rice.cs.hpcremote;

import edu.rice.cs.hpcbase.IDatabaseIdentification;

public class RemoteDatabaseIdentification implements IDatabaseIdentification 
{
	private static final String EMPTY = "";

	private String databasePath;
	private String host;
	private int port;
	
	private String username;

	
	public RemoteDatabaseIdentification() {
		databasePath = EMPTY;
		host = EMPTY;
		port = 8080;
		username = EMPTY;
	}
	
	
	public RemoteDatabaseIdentification(String databaseId) {
		username = EMPTY;
		databasePath = EMPTY;
		
		var colon = databaseId.indexOf(':');
		var slash = databaseId.indexOf('/');
		if (colon >= slash)
			return;
		
		host = databaseId.substring(0, colon); 
		var portStr  = databaseId.substring(colon+1, slash);
		
		if (colon <= 0 && slash < 1 || portStr.isEmpty())
			return;
		
		try {
			port = Integer.parseInt(portStr);
		} catch (NumberFormatException e) {
			// it is not a remote or invalid port number
			throw new IllegalArgumentException("Invalid port number: " + portStr);
		}
	}
	
	public RemoteDatabaseIdentification(String host, int port) {
		this(host, port, EMPTY, EMPTY);
	}
	
	public RemoteDatabaseIdentification(String host, int port, String path, String username) {
		this.host = host;
		this.port = port;
		
		this.databasePath = path;
		this.username = username;
	}
	
	
	public String getHost() {
		return host;
	}
	
	
	public int getPort() {
		return port;
	}
	
	
	public String getPath() {
		return databasePath;
	}
	
	
	public String getUsername() {
		return username;
	}
	
	@Override
	public String id() {
		StringBuilder sb = new StringBuilder();
		if (username != null && !username.isEmpty()) {
			sb.append(username);
			sb.append("@");
		}
		if (host != null)
			sb.append(host);
		if (port > 1) {
			sb.append(':');
			sb.append(port);
		}
		sb.append('/');
		
		if (databasePath != null)
			sb.append(databasePath);
		
		return sb.toString();
	}		
	
	
	@Override
	public String toString() {
		return id();
	}
	
	
	@Override
	public int hashCode() {
		return databasePath.hashCode();
	}
	
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof IDatabaseIdentification) {
			IDatabaseIdentification other = (IDatabaseIdentification) o;
			return id().equals(other.id());
		}
		return false;
	}
}