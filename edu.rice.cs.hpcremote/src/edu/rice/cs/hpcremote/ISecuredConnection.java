package edu.rice.cs.hpcremote;

import java.io.IOException;
import java.io.InputStream;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public interface ISecuredConnection 
{
	boolean connect(String username, String hostName);
	
	ISessionRemoteCommand executeRemoteCommand(String command);
	
	ISocketSession socketForwarding(String socketPath);
	
	
	/***
	 * 
	 * Base interface to start a communication session
	 *
	 */
	interface ISession
	{
		/**
		 * Get the current communication session.
		 * 
		 * @return
		 * @throws JSchException
		 */
		Session getSession() throws JSchException;
		
		/****
		 * Disconnect the session
		 */
		void disconnect();
	}
	
	
	/****
	 * 
	 * Interface to start the remote command execution
	 *
	 */
	interface ISessionRemoteCommand extends ISession
	{
		/**
		 * Get the output of the command execution,
		 * which is our input (in our perspective)
		 * 
		 * @return
		 * @throws IOException
		 */
		InputStream getInputStream() throws IOException;
		
		/***
		 * Get the standard output of the execution
		 * @return
		 * @throws IOException 
		 */
		String getCurrentStandardOutput() throws IOException;
	}
	
	interface ISocketSession extends ISession 
	{
		int getLocalPort();
	}
}
