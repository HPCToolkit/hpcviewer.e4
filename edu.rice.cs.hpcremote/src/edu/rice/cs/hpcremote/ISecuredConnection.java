package edu.rice.cs.hpcremote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public interface ISecuredConnection 
{

	boolean connect(IConnection connectionInfo);

	void close();
	
	ISessionRemote executeRemoteCommand(String command);
	
	ISessionRemoteSocket socketForwarding(String socketPath);
	
	
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
	interface ISessionRemote extends ISession
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
		 * Get the array of text messages from the remote host
		 * 
		 * @return {@code String[]}
		 * 			array of text messages
		 * 
		 * @throws IOException 
		 */
		default String[] read() throws IOException {
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
			if (tokenizer.countTokens() == 0) {
				writeLog("\t RECV: " + text);
				return new String[0];
			}
			String []listTexts = new String[tokenizer.countTokens()];
			
			for(int i=0; tokenizer.hasMoreTokens(); i++) {
				listTexts[i] = tokenizer.nextToken();
			}
			writeLog("\t RECV: " + listTexts[0] + " out of " + listTexts.length);
			
			return listTexts;
		}

		
		/****
		 * send a text message to the remote host
		 * 
		 * @param message 
		 * 			Text to be sent
		 * 
		 * @throws IOException
		 */
		default void write(String message) throws IOException {
			var stream = getLocalOutputStream();
			byte []msgBytes = message.getBytes();
			
			writeLog("SEND " + message);
			
			stream.write(msgBytes);
		}
		
		
		/***
		 * Default implementation to write a log
		 * @param text
		 */
		default void writeLog(String text) {
			System.err.println(text);
		}
	}
	
	
	/****
	 * 
	 * Interface to communicate with the remote UNIX socket
	 *
	 */
	interface ISessionRemoteSocket extends ISessionRemote 
	{
		/****
		 * Get the SSH tunneled of the local port that connects
		 * with the remote UNIX socket.
		 * 
		 * @return {@code int}
		 * 			The local port, or negative number if it fails.
		 */
		int getLocalPort();
	}
}