package edu.rice.cs.hpcremote;

import edu.rice.cs.hpcbase.IDatabaseIdentification;

public class RemoteDatabaseIdentification implements IDatabaseIdentification 
{
	private static final String EMPTY = "";

	private String path;
	private String host;
	private int port;
	
	private String username;

	public RemoteDatabaseIdentification(String databaseId) {
		username = EMPTY;
		path = EMPTY;
		
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
		
		this.path = path;
		this.username = username;
	}
	
	
	public String getHost() {
		return host;
	}
	
	
	public int getPort() {
		return port;
	}
	
	
	public String getPath() {
		return path;
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
		
		if (path != null)
			sb.append(path);
		
		return sb.toString();
	}		
}
