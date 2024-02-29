package edu.rice.cs.hpcremote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

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
		OutputStream getLocalOutputStream() throws IOException;
		
		/**
		 * Get the output of the command execution,
		 * which is our input (in our perspective)
		 * 
		 * @return
		 * @throws IOException
		 */
		InputStream getLocalInputStream() throws IOException;
		
		/***
		 * Get the standard output of the execution
		 * @return
		 * @throws IOException 
		 */
		default String[] getCurrentLocalInput() throws IOException {
			StringBuilder inputs = new StringBuilder();
			final var  inStream  = getLocalInputStream();
			
			final int MAX_BYTES = 1024;
			
			byte[] tmp = new byte[MAX_BYTES];
			
			while (inStream.available() > 0) {
				int i = inStream.read(tmp, 0, MAX_BYTES);
				if (i < 0)
					break;
				var s = new String(tmp, 0, i);
				inputs.append(s);
			}
			var text = inputs.toString();
			StringTokenizer tokenizer = new StringTokenizer(text, "\n");
			String []listTexts = new String[tokenizer.countTokens()];
			
			for(int i=0; tokenizer.hasMoreTokens(); i++) {
				listTexts[i] = tokenizer.nextToken();
			}
			System.err.println("\t RECV: " + listTexts[0] + " out of " + listTexts.length);
			
			return listTexts;
		}

		default void writeLocalOutput(String message) throws IOException {
			var stream = getLocalOutputStream();
			byte []msgBytes = message.getBytes();
			
			System.err.println("SEND " + message);
			
			stream.write(msgBytes);
		}
	}
	
	interface ISocketSession extends ISessionRemoteCommand 
	{
		int getLocalPort();
	}
}
