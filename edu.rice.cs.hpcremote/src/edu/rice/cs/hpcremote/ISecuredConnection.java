// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcremote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;


public interface ISecuredConnection 
{
	/***
	 * For a given connection object, tries to connect to the remote host
	 * either using SSH or other means.
	 * 
	 * @param connectionInfo
	 * 
	 * @return {@code boolean}
	 * 			true if the connection succeeds, false otherwise.
	 */
	boolean connect(IConnection connectionInfo);

	/***
	 * Disconnect the existing connection.
	 */
	void close();
	
	
	/***
	 * Execute a command to the remote host.
	 * 
	 * @param command
	 * 
	 * @return {@code ISessionRemote}
	 * 			The new session if the execution is successful, null otherwise.
	 */
	ISessionRemote executeRemoteCommand(String command);
	
	
	/***
	 * Create a new session to manage UNIX domain socket forwarding.
	 *  
	 * @param socketPath
	 * 			A non-existing UNIX socket (will be created by the server).
	 * 
	 * @return {@code ISessionRemoteSocket}
	 * 			A new remote session
	 */
	ISessionRemoteSocket socketForwarding(String socketPath);
	

	/***
	 * Add listener to error message from the remote host.
	 * This method is called when the remote flush a new error message.
	 * 
	 * This is optional, client can decide to ignore error messages
	 * (not recommended though)
	 * 
	 * @param handler
	 */
	void addErrorMessageHandler(IErrorMessageHandler handler);

	
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
		final boolean DEBUG_MODE = ViewerPreferenceManager.INSTANCE.getDebugMode();
		
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
			if (inputs.length() == 0)
				return new String[0];
			
			var text = inputs.toString();
			StringTokenizer tokenizer = new StringTokenizer(text, "\n");
			if (tokenizer.countTokens() == 0)
				return new String[] {text};

			String []listTexts = new String[tokenizer.countTokens()];
			
			for(int i=0; tokenizer.hasMoreTokens(); i++) {
				listTexts[i] = tokenizer.nextToken();
			}
			writeLog("\t RECV: " + inputs.substring(0, Math.min(100, inputs.length())).replace('\n', ' ') + " / " + listTexts.length);
			
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
			if (DEBUG_MODE)
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

	
	/****
	 * Interface to handle remote host's error message
	 */
	interface IErrorMessageHandler 
	{
		void message(String errMsg);
	}
}