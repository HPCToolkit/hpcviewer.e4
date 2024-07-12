// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcremote.tunnel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import edu.rice.cs.hpcremote.ISecuredConnection.ISessionRemoteSocket;

public class SocketForwardingSession implements ISessionRemoteSocket 
{
	private static final int TIMEOUT_DEFAULT = 10 * 1000;

	private final Session session;
	private final int localPort;
	
	private final Socket socket;

	
	public SocketForwardingSession(Session session, String socketPath) throws JSchException, IOException {
		this.session = session;
		localPort = session.setSocketForwardingL(null, 0, socketPath, null, TIMEOUT_DEFAULT);
		socket = new Socket("localhost", localPort);
	}

	@Override
	public OutputStream getLocalOutputStream() throws IOException {
		return socket.getOutputStream();
	}
	
	
	@Override
	public InputStream getLocalInputStream() throws IOException {
		return socket.getInputStream();
	}
	
	/***
	 * Wrapper implementation of {@code read} to try to read the input from the server:
	 * <br/>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public String[] read() throws IOException {
		String []lines = null;
		int numAttempts = 50;
		while (numAttempts > 0) {
			lines = ISessionRemoteSocket.super.read();
			if (lines != null && lines.length > 0)
				break;
			
			numAttempts--;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// ignore it ?
			    Thread.currentThread().interrupt();
			}
		}
		return lines;
	}
	
	@Override
	public Session getSession() throws JSchException {
		return session;
	}

	@Override
	public void disconnect() {
		try {
			socket.close();
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).error("Unable to close socket", e);
		}
	}

	@Override
	public int getLocalPort() {
		return localPort;
	}
	
	@Override
	public void writeLog(String text) {
		ISessionRemoteSocket.super.writeLog(localPort + ": " + text);
	}
}
