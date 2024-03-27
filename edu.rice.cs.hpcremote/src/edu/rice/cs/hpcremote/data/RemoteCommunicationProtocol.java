package edu.rice.cs.hpcremote.data;

import java.io.IOException;
import java.net.InetAddress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;
import org.hpctoolkit.hpcclient.v1_0.HpcClientJavaNetHttp;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcremote.ICollectionOfConnections;
import edu.rice.cs.hpcremote.IConnection;
import edu.rice.cs.hpcremote.IRemoteCommunicationProtocol;
import edu.rice.cs.hpcremote.ISecuredConnection;
import edu.rice.cs.hpcremote.tunnel.SecuredConnectionSSH;
import edu.rice.cs.hpcremote.ui.ConnectionDialog;
import edu.rice.cs.hpcremote.ui.RemoteDatabaseDialog;

public class RemoteCommunicationProtocol implements IRemoteDirectoryBrowser, IRemoteCommunicationProtocol
{	
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
		return connection.getUsername();
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

		// check if we already have exactly the same connection as the 
		// requested host, user id and installation
		
		var setOfConnections = ICollectionOfConnections.getShellSessions(shell);
		if (setOfConnections.containsKey(connectionDialog.getId())) {
			var matchedConnection = setOfConnections.get(connectionDialog.getId());
			
			if (matchedConnection != null) {
				// copy and then reuse the existing session which has exactly the same
				// remote host, user and installation
				serverMainSession = matchedConnection.serverMainSession;
				connection = matchedConnection.connection;
				remoteIP = matchedConnection.remoteIP;
				remoteSocket = matchedConnection.remoteSocket;
				
				return ConnectionStatus.CONNECTED;
			}
		}
		
		//
		// launching hpcserver on the remote host
		//
		var connectionSSH = new SecuredConnectionSSH(shell);		
		if (!connectionSSH.connect(
				connectionDialog.getUsername(), 
				connectionDialog.getHost(), 
				connectionDialog.getPrivateKey()))
			return ConnectionStatus.ERROR;
		
		String command = connectionDialog.getInstallationDirectory() + "/bin/hpcserver.sh" ;
		
		var remoteSession = connectionSSH.executeRemoteCommand(command);
		if (remoteSession == null) 
			return ConnectionStatus.ERROR;
		
		if (!handleOutputAndSetConfiguration(remoteSession))
			return ConnectionStatus.ERROR;
	
		//
		// create the SSH tunnel to communicate securely with the remote host
		//
		serverMainSession = connectionSSH.socketForwarding(remoteSocket);
		if (serverMainSession == null)
			return ConnectionStatus.ERROR;
		
		this.connection = connectionDialog;
		
		ICollectionOfConnections.putShellSession(shell, connection.getId(), this);
		
		return ConnectionStatus.CONNECTED;
	}

	
	@Override
	public void disconnect(Shell shell) throws IOException {
		if (serverMainSession == null)
			return;
		
		serverMainSession.write("@QUIT");
		serverMainSession.disconnect();
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
		if (serverMainSession == null)
			throw new IllegalAccessError("SSH tunnel not created yet.");
		
		String message = directory.isEmpty() ? "@LIST" : "@LIST " + directory;
		serverMainSession.write(message.trim());
		
		var listDir = serverMainSession.read();
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
	public IRemoteDatabaseConnection openDatabaseConnection(Shell shell, String database) throws IOException {
		if (serverMainSession == null || connection == null)
			return null;
		
		serverMainSession.write("@DATA " + database);
		
		var inputs = serverMainSession.read();
		if (inputs == null || inputs.length == 0)
			return null;
		
		var response = inputs[0];
		if (response.startsWith("@ERR")) {
			throw new IOException("Error reading database " + database + ": " + response.substring(5));
		
		} else if (response.startsWith("@SOCK")) {
			var brokerSocket = response.substring(6);
			return createTunnelAndRequestDatabase(shell, brokerSocket);
			
		} else {
			throw new UnknownError("Unknown response from the remote host: " + response);
		}
	}

	
	private IRemoteDatabaseConnection createTunnelAndRequestDatabase(Shell shell, String brokerSocket) throws IOException {
		var brokerSSH = new SecuredConnectionSSH(shell);
		
		if (!brokerSSH.connect(connection.getUsername(), connection.getHost(), connection.getPrivateKey()))
			return null;
		
		var brokerSession = brokerSSH.socketForwarding(brokerSocket);
		if (brokerSession == null)
			return null;
		
		var inputs = serverMainSession.read();
		if (inputs != null && inputs.length > 0 &&  (inputs[0].startsWith("@ERR"))) {
				throw new IOException(inputs[0].substring(5));
		}
		
		int port = brokerSession.getLocalPort();
		var addr = InetAddress.getByName("localhost");
		final var hpcclient = new HpcClientJavaNetHttp(addr, port);
		
		return new IRemoteDatabaseConnection() {
			
			@Override
			public HpcClient getHpcClient() {
				return hpcclient;
			}
			
			@Override
			public ISecuredConnection getConnection() {
				return brokerSSH;
			}
			
			@Override
			public ISecuredConnection.ISessionRemoteSocket getRemoteSocket() {
				return brokerSession;
			}
		};
	}
	
		
	private boolean handleOutputAndSetConfiguration(ISecuredConnection.ISessionRemote session ) 
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
			
			var output = session.read();
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
					LoggerFactory.getLogger(getClass()).debug("Unknown remote command: " + pair);
				}
			}
		}
		return true;
	}
}
