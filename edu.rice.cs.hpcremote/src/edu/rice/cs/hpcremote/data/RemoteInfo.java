package edu.rice.cs.hpcremote.data;

import org.hpctoolkit.hpcclient.v1_0.HpcClient;

import edu.rice.cs.hpcremote.IRemoteInfo;

public class RemoteInfo implements IRemoteInfo 
{
	private String host;
	private int port;
	
	private String username;
	
	private HpcClient client;

	@Override
	public String getId() {
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
		
		return sb.toString();
	}

	@Override
	public HpcClient getClient() {
		return client;
	}

	
	public void setHostAndPort(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	
	public void setClient(HpcClient client) {
		this.client = client;
	}
}
