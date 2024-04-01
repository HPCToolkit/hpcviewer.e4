package edu.rice.cs.hpcremote.tunnel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import edu.rice.cs.hpcremote.ISecuredConnection.ISessionRemoteSocket;

public class SocketForwardingSession implements ISessionRemoteSocket 
{
	private static final String END_OF_MESSAGE_SIGN = "@EOM";

	private static final int TIMEOUT_DEFAULT = 10 * 1000;

	private final Session session;
	private final int localPort;
	
	private final Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	
	public SocketForwardingSession(Session session, String socketPath) throws JSchException, IOException {
		this.session = session;
		localPort = session.setSocketForwardingL(null, 0, socketPath, null, TIMEOUT_DEFAULT);
		
		socket = new Socket("localhost", localPort);
		out = new PrintWriter(socket.getOutputStream(), true);
		in  = new BufferedReader( new InputStreamReader(socket.getInputStream()));
	}

	@Override
	public OutputStream getLocalOutputStream() throws IOException {
		return socket.getOutputStream();
	}

	
	@Override
	public void write(String message) throws IOException {
		log("SEND " + message);
		
		out.println(message + "\n");
		out.flush();
	}
	
	@Override
	public InputStream getLocalInputStream() throws IOException {
		return socket.getInputStream();
	}
	
	@Override
	public String[] read() throws IOException {
		var list = new ArrayList<String>();
        while(true) {
        	var line = in.readLine();
        	if (line.startsWith(END_OF_MESSAGE_SIGN) || line.isEmpty())
        		break;
        	list.add(line);
        }
		log("\t RECV " + list.get(0) + " / " + list.size());
		
        String []texts = new String[list.size()];
		return list.toArray(texts);
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

	
	private void log(String message) {
		System.err.println(message);
	}
}
