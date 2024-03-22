package edu.rice.cs.hpcremote.tunnel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import edu.rice.cs.hpcremote.ISecuredConnection;
import edu.rice.cs.hpcremote.ui.RemoteUserInfoDialog;


public class SecuredConnectionSSH implements ISecuredConnection 
{
	private final Shell shell;
	private Session session;
	
	public SecuredConnectionSSH(Shell shell) {
		this.shell = shell;
	}

	
	@Override
	public boolean connect(String username, String hostName) {
		return connect(username, hostName, null);
	}

	
	@Override
	public boolean connect(String username, String hostName, String privateKey) {
		var userInfo = new RemoteUserInfoDialog(shell);
		
		JSch jsch = new JSch();
		try {
			jsch.setKnownHosts("~/.ssh/known_hosts");

			if (privateKey != null && !privateKey.isEmpty())
				jsch.addIdentity(privateKey);
			
			// may throw an exception
			session = jsch.getSession(username, hostName, 22);
			
			session.setUserInfo(userInfo);
			
			// this may throw an exception
			session.connect();
			
		} catch (JSchException e) {
			MessageDialog.openError(shell, "Unabe to connect " + hostName, e.getLocalizedMessage());
			return false;
		}
		return true;
	}
	
	@Override
	public ISessionRemote executeRemoteCommand(String command) {
		if (session == null)
			throw new IllegalAccessError("Not connected. Need to call connect() first");
		
		try {
			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
			return executeCommand(channelExec, command);
			
		} catch (JSchException | IOException e) {
			MessageDialog.openError(shell, "Fail to execute remote command", e.getLocalizedMessage());
			return null;
		}
	}

	
	@Override
	public ISessionRemoteSocket socketForwarding(String socketPath) {
		if (session == null)
			throw new IllegalAccessError("Not connected. Need to call connect() first");

		try {
			return new SocketForwardingSession(session, socketPath);

		} catch (JSchException | IOException e) {
			MessageDialog.openError(
					shell, 
					"Fail to create SSH tunnel", 
					socketPath + ": " +  e.getLocalizedMessage());
		}
		return null;
	}

	
	public String getHost() {
		if (session == null)
			throw new IllegalAccessError("Host is not connected");
		
		return session.getHost();
	}
	
	
	public String getUsername() {
		if (session == null)
			throw new IllegalAccessError("Host is not connected");

		return session.getUserName();
	}
	
	private ISessionRemote executeCommand(ChannelExec channel, String command) throws JSchException, IOException {
		channel.setCommand(command);
		
		channel.setInputStream(null);
		
		//ByteArrayOutputStream errStream = new ByteArrayOutputStream();		
		channel.setErrStream(System.err);
	
		final var inStream = channel.getInputStream();
		
		// need to call connect to execute the command
		channel.connect();
		
		return new ISessionRemote() {
			
			@Override
			public Session getSession() throws JSchException {
				return channel.getSession();
			}
			
			@Override
			public void disconnect() {
				channel.disconnect();
			}
			
			@Override
			public InputStream getLocalInputStream() throws IOException {
				return inStream;
			}

			@Override
			public OutputStream getLocalOutputStream() throws IOException {
				return channel.getOutputStream();
			}
		};
	}


	@Override
	public void close() {
		if (session != null)
			session.disconnect();
	}
}