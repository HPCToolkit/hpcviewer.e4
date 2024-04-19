package edu.rice.cs.hpcremote.data;

import java.io.IOException;
import java.net.InetAddress;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;
import org.hpctoolkit.hpcclient.v1_0.HpcClientJavaNetHttp;
import edu.rice.cs.hpcremote.ICollectionOfConnections;
import edu.rice.cs.hpcremote.IConnection;
import edu.rice.cs.hpcremote.IRemoteCommunicationProtocol;
import edu.rice.cs.hpcremote.ISecuredConnection;
import edu.rice.cs.hpcremote.tunnel.SecuredConnectionSSH;
import edu.rice.cs.hpcremote.ui.ConnectionDialog;
import edu.rice.cs.hpcremote.ui.RemoteDatabaseDialog;

public abstract class RemoteCommunicationProtocolBase 
	implements IRemoteCommunicationProtocol, IRemoteDirectoryBrowser
{
	enum ServerResponseType {SUCCESS, ERROR, INVALID}
	
	private final IllegalAccessError errorNotConnected = new IllegalAccessError("SSH tunnel not created yet.");
	
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
	
	
	
	@Override
	public void disconnect(Shell shell) throws IOException {
		if (serverMainSession == null)
			throw errorNotConnected;
		
		disconnect(serverMainSession, shell);
		serverMainSession.disconnect();
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
	public String selectDatabase(Shell shell) {
		if (serverMainSession == null)
			throw errorNotConnected;
		
		var dialog = new RemoteDatabaseDialog(shell, this);
		if (dialog.open() == Window.OK) {
			return dialog.getSelectedDirectory();
		}		
		return null;
	}

	

	@Override
	public IRemoteDatabaseConnection openDatabaseConnection(Shell shell, String database) throws IOException {
		if (serverMainSession == null || getConnection() == null)
			throw errorNotConnected;

		var reply = sendCommandToOpenDatabaseAndWaitForReply(serverMainSession, database);
		
		switch (reply.getResponseType()) {
		case ERROR: 
			throw new IOException("Error reading the database: " + database);
		case INVALID:
			throw new UnknownError("Fail to connect to the server.");
		case SUCCESS:
			var socket = reply.getResponseArgument()[0];
			return createTunnelAndRequestDatabase(shell, socket);
		}
		return null;
	}
	
	
	@Override
	public IRemoteDirectoryContent getContentRemoteDirectory(String directory) throws IOException {
		if (serverMainSession == null)
			throw errorNotConnected;
		
		var response = sendCommandToGetDirectoryContent(serverMainSession, directory);
		
		switch (response.getResponseType()) {
		case ERROR:
			throw new IOException("Fail reading directory: " + directory);
		case INVALID:
			throw new UnknownError("Fail to connect to the server.");
		case SUCCESS:
			var list = response.getResponseArgument();
			return createDirectoryContent(serverMainSession, list);
		}
		return null;
	}
	
	
	IRemoteDatabaseConnection createTunnelAndRequestDatabase(Shell shell, String brokerSocket) throws IOException {
		var brokerSSH = new SecuredConnectionSSH(shell);
		
		if (!brokerSSH.connect(connection.getUsername(), connection.getHost(), connection.getPrivateKey()))
			return null;
		
		var brokerSession = brokerSSH.socketForwarding(brokerSocket);
		if (brokerSession == null)
			return null;
		
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
			var response = getServerResponseInit(output);
			if (response == null || response.getResponseType() != ServerResponseType.SUCCESS)
				return false;
			
			remoteIP = response.getHost();
			remoteSocket = response.getSocket();
		}
		return true;
	}

	
	IConnection getConnection() {
		return connection;
	}

	
	/***
	 * Handle the message from the server initial connection
	 * 
	 * @param messageFromServer
	 * 
	 * @return {@code ServerResponseConnectionInit} 
	 */
	public abstract ServerResponseConnectionInit getServerResponseInit(String []messageFromServer);
	
	/****
	 * Ask the server to open a database and wait for its reply.
	 * 
	 * @param serverMainSession
	 * @param database
	 * @return
	 * @throws IOException
	 */
	public abstract ServerResponse sendCommandToOpenDatabaseAndWaitForReply(
			ISecuredConnection.ISessionRemoteSocket serverMainSession, 
			String database) 
					throws IOException;
	
	
	/***
	 * Ask the server to get the content of a directory and wait for its reply.
	 * 
	 * @param directory
	 * @return
	 * @throws IOException
	 */
	public abstract ServerResponse sendCommandToGetDirectoryContent(
			ISecuredConnection.ISessionRemoteSocket serverMainSession, 
			String directory) 
					throws IOException;
	
	
	/***
	 * Convert the response from the server to a {@code IRemoteDirectoryContent}
	 * 
	 * @param responseFromServer
	 * 			{@code String[]} array of String from the server
	 * @return
	 */
	public abstract IRemoteDirectoryContent createDirectoryContent(
			ISecuredConnection.ISessionRemoteSocket serverMainSession, 
			String[] responseFromServer);
	
	
	/***
	 * Send a command to close the connection to the server
	 * 
	 * @param serverMainSession
	 * @param shell
	 * @throws IOException
	 */
	public abstract void disconnect(
			ISecuredConnection.ISessionRemoteSocket serverMainSession, 
			Shell shell) throws IOException;
	
	public interface ServerResponse 
	{
		ServerResponseType getResponseType();
		
		String[] getResponseArgument();
		
		ServerResponse INVALID = new ServerResponse() {

			@Override
			public ServerResponseType getResponseType() {
				return ServerResponseType.INVALID;
			}

			@Override
			public String[] getResponseArgument() {
				return new String[0];
			}
			
		};
	}
	
	public interface ServerResponseConnectionInit extends ServerResponse
	{
		String getSocket();
		
		String getHost();
	}
}
