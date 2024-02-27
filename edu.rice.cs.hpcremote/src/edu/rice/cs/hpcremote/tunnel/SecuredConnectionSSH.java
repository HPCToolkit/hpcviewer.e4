package edu.rice.cs.hpcremote.tunnel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
	
	public boolean connect(String username, String hostName) {
		var userInfo = new RemoteUserInfoDialog(shell);
		
		JSch jsch = new JSch();
		try {
			// may throw an exception
			session = jsch.getSession(username, hostName, 22);
			
			session.setUserInfo(userInfo);
			
			// this may throw an exception
			session.connect();
			
		} catch (JSchException e) {
			MessageDialog.openError(shell, "Error to connect " + hostName, e.getLocalizedMessage());
			return false;
		}
		return true;
	}

	
	@Override
	public ISessionRemoteCommand executeRemoteCommand(String command) {
		if (session == null)
			throw new IllegalAccessError("Not connected. Need to call connect() first");
		
		try {
			ChannelExec channel = (ChannelExec) session.openChannel("exec");
			return executeCommand(channel, command);
			
		} catch (JSchException | IOException e) {
			MessageDialog.openError(shell, "Fail to execute remote command", e.getLocalizedMessage());
			return null;
		}
	}

	
	@Override
	public ISocketSession socketForwarding(String socketPath) {
		return null;
	}

	
	private ISessionRemoteCommand executeCommand(ChannelExec channel, String command) throws JSchException, IOException {
		channel.setCommand(command);
		
		channel.setInputStream(null);
		
		ByteArrayOutputStream errStream = new ByteArrayOutputStream();		
		channel.setErrStream(errStream);
	
		final var inStream = channel.getInputStream();
		
		// need to call connect to execute the command
		channel.connect();
		
		return new ISessionRemoteCommand() {
			
			@Override
			public Session getSession() throws JSchException {
				return channel.getSession();
			}
			
			@Override
			public void disconnect() {
				channel.disconnect();
			}
			
			@Override
			public InputStream getInputStream() throws IOException {
				return inStream;
			}
			
			@Override
			public String getCurrentStandardOutput() throws IOException {
				return readInput();
			}			
			
			private String readInput() throws IOException {
				StringBuilder inputs = new StringBuilder();
				
				final int MAX_BYTES = 1024;
				
				byte[] tmp = new byte[MAX_BYTES];
				
				while (inStream.available() > 0) {
					int i = inStream.read(tmp, 0, MAX_BYTES);
					if (i < 0)
						break;
					var s = new String(tmp, 0, i);
					inputs.append(s);
				}
				return inputs.toString();
			}
		};
	}
}