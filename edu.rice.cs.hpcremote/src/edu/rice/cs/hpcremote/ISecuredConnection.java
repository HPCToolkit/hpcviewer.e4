package edu.rice.cs.hpcremote;

import java.io.InputStream;

import com.jcraft.jsch.Session;

public interface ISecuredConnection 
{
	public void connect(String username, String hostName);
	
	ISessionRemoteCommand executeRemoteCommand(String command);
	
	ISocketSession socketForwarding(String socketPath);
	
	
	interface ISession
	{
		Session getSession();
		
		void disconnect();
	}
	
	interface ISessionRemoteCommand extends ISession
	{
		InputStream getInputStream();
	}
	
	interface ISocketSession extends ISession 
	{
		int getLocalPort();
	}
}
