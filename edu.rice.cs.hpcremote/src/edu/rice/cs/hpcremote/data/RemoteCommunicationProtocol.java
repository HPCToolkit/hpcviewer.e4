package edu.rice.cs.hpcremote.data;

import java.io.IOException;
import java.net.InetAddress;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;
import org.hpctoolkit.hpcclient.v1_0.HpcClientJavaNetHttp;

import edu.rice.cs.hpcremote.IConnection;
import edu.rice.cs.hpcremote.IRemoteCommunicationProtocol;
import edu.rice.cs.hpcremote.ISecuredConnection;
import edu.rice.cs.hpcremote.tunnel.SecuredConnectionSSH;
import edu.rice.cs.hpcremote.ui.ConnectionDialog;
import edu.rice.cs.hpcremote.ui.RemoteDatabaseDialog;

public class RemoteCommunicationProtocol implements IRemoteDirectoryBrowser, IRemoteCommunicationProtocol
{	
	private SecuredConnectionSSH connectionSSH;
	private ISecuredConnection.ISessionRemoteSocket serverMainSession;
	
	private String remoteIP;
	private String remoteSocket;
	
	private IConnection connection;

	
	@Override
	public String getRemoteHost() {
		return remoteIP;
	}
	
	
	@Override
	public String getUsername() {
		return connectionSSH.getUsername();
	}
	
	/*****
	 * Connect to a remote host
	 * @param shell
	 * @param connection
	 * @return
	 * @throws IOException
	 */
	@Override
	public ConnectionStatus connect(Shell shell) throws IOException {
		if (serverMainSession != null)
			return ConnectionStatus.CONNECTED;
		
		var connectionDialog = new ConnectionDialog(shell);
		if (connectionDialog.open() == Window.CANCEL) {
			return ConnectionStatus.NOT_CONNECTED;
		}
		
		if (connectionSSH == null)
			connectionSSH = new SecuredConnectionSSH(shell);
		
		if (!connectionSSH.connect(connectionDialog.getUsername(), connectionDialog.getHost()))
			return ConnectionStatus.ERROR;
		
		String command = connectionDialog.getInstallationDirectory() + "/bin/hpcserver.sh" ;
		
		var remoteSession = connectionSSH.executeRemoteCommand(command);
		if (remoteSession == null) 
			return ConnectionStatus.ERROR;
		
		if (!handleRemoteCommandOutput(remoteSession))
			return ConnectionStatus.ERROR;
	
		serverMainSession = connectionSSH.socketForwarding(remoteSocket);
		if (serverMainSession == null)
			return ConnectionStatus.ERROR;
		
		this.connection = connectionDialog;
		
		return ConnectionStatus.CONNECTED;
	}

	
	@Override
	public String selectDatabase(Shell shell) {
		if (serverMainSession == null)
			throw new IllegalAccessError("SSH tunnel not created yet.");
		
		var dialog = new RemoteDatabaseDialog(shell, this);
		if (dialog.open() == Window.OK) {
			return dialog.getSelectedDirectory();
		}		
		return null;
	}
	

	@Override
	public IRemoteDirectoryContent getContentRemoteDirectory(String directory) throws IOException {
		serverMainSession.writeLocalOutput("@LIST " + directory);
		var listDir = serverMainSession.getCurrentLocalInput();
		if (listDir == null || listDir.length == 0)
			return null;
		
		if (listDir[0].startsWith("@LIST")) {
			var currentDir = listDir[0].substring(6);
			String []content = new String[listDir.length-1];
			for(int i=0; i<content.length; i++) {
				content[i] = listDir[i+1];
			}
			return new IRemoteDirectoryContent() {
				
				@Override
				public String getDirectory() {
					return currentDir;
				}
				
				@Override
				public String[] getContent() {
					return content;
				}
			};
		} else if (listDir[0].startsWith("@ERR")) {
			throw new IOException("Error reading directory: " + listDir[0].substring(5));
		}
		return null;
	}
	
	
	@Override
	public HpcClient openDatabaseConnection(Shell shell, String database) throws IOException {
		if (serverMainSession == null || connection == null)
			return null;
		
		serverMainSession.writeLocalOutput("@DATA " + database);
		
		var inputs = serverMainSession.getCurrentLocalInput();
		if (inputs == null || inputs.length == 0)
			return null;
		
		var response = inputs[0];
		if (response.startsWith("@ERR")) 
			throw new IOException("Error reading database " + database + ": " + response.substring(5));
		
		if (response.startsWith("@SOCK")) {
			var brokerSocket = response.substring(6);
			
			var brokerSSH = new SecuredConnectionSSH(shell);
			if (brokerSSH.connect(connection.getUsername(), connection.getHost())) {
				var brokerSession = brokerSSH.socketForwarding(brokerSocket);
				if (brokerSession == null)
					return null;
				
				int port = brokerSession.getLocalPort();
				var addr = InetAddress.getByName("localhost");
				
				return new HpcClientJavaNetHttp(addr, port);
			}
		}
		return null;
	}
	
	private boolean handleRemoteCommandOutput(ISecuredConnection.ISessionRemote session ) 
			throws IOException {

		int maxAttempt = 10;

		remoteIP = "";
		remoteSocket = "";

		while (remoteIP.isEmpty() && remoteSocket.isEmpty()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			    // Restore interrupted state...
			    Thread.currentThread().interrupt();
			}
			
			var output = session.getCurrentLocalInput();
			if (output == null || output.length == 0) {
				maxAttempt--;
				if (maxAttempt == 0)
					return false;
				
				continue;
			}
			
			for (int i=0; i<output.length; i++) {
				var pair = output[i];
				if (pair.startsWith("@HOST")) {
					remoteIP = pair.substring(6); 
				} else if (pair.startsWith("@SOCK")) {
					remoteSocket = pair.substring(6);
				} else {
					return false;
				}
			}
		}
		return true;
	}

}
