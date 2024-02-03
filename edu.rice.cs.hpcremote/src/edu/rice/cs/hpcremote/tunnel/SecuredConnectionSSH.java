package edu.rice.cs.hpcremote.tunnel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import edu.rice.cs.hpcremote.ISecuredConnection;
import edu.rice.cs.hpcremote.ui.RemoteConnectionDialog;

public class SecuredConnectionSSH implements ISecuredConnection 
{
	private final Shell shell;
	private Session session;
	
	public SecuredConnectionSSH(Shell shell) {
		this.shell = shell;
	}
	
	public void connect(String username, String hostName) {
		var userInfo = new RemoteConnectionDialog(shell);
		
		JSch jsch = new JSch();
		try {
			// may throw an exception
			session = jsch.getSession(username, hostName);
			
			session.setUserInfo(userInfo);
			
			// this may throw an exception
			session.connect();
			
		} catch (JSchException e) {
			MessageDialog.openError(shell, "Error to connect " + hostName, e.getLocalizedMessage());
		}
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

	
	private ISessionRemoteCommand executeCommand(ChannelExec channel, String command) throws IOException, JSchException {
		channel.setInputStream(null);
		
		ByteArrayOutputStream errStream = new ByteArrayOutputStream();		
		channel.setErrStream(errStream);
		
		
		channel.connect();
	
		readInput(channel);
		
		return null;
	}
	
	
	private List<String> readInput(ChannelExec channel) throws IOException {
		var inStream = channel.getInputStream();

		List<String> inputs = new ArrayList<>();
		
		final int MAX_BYTES = 1024;
		
		byte[] tmp = new byte[MAX_BYTES];
		
		while (true) {
			while (inStream.available() > 0) {
				int i = inStream.read(tmp, 0, MAX_BYTES);
				if (i < 0)
					break;
				System.out.print(new String(tmp, 0, i));
			}
			if (channel.isClosed()) {
				if (inStream.available() > 0)
					continue;
				break;
			}
		}
		return inputs;
	}
}