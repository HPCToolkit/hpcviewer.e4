// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.data;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.NotYetConnectedException;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.hpctoolkit.hpcclient.v1_0.BrokerClient;
import org.hpctoolkit.hpcclient.v1_0.BrokerClientJavaNetHttp;
import org.hpctoolkit.hpcclient.v1_0.DbManagerClient;
import org.hpctoolkit.hpcclient.v1_0.DbManagerClientJavaNetHttp;
import org.hpctoolkit.hpcclient.v1_0.DirectoryContentsNotAvailableException;
import org.hpctoolkit.hpcclient.v1_0.RemoteDirectory;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import edu.rice.cs.hpcremote.ICollectionOfConnections;
import edu.rice.cs.hpcremote.IConnection;
import edu.rice.cs.hpcremote.ISecuredConnection;
import edu.rice.cs.hpcremote.data.IServerResponse.ServerResponseType;
import edu.rice.cs.hpcremote.tunnel.IRemoteCommunication;
import edu.rice.cs.hpcremote.tunnel.SecuredConnectionSSH;
import edu.rice.cs.hpcremote.ui.RemoteDatabaseDialog;

public class RemoteCommunicationProtocolBase 
	implements IRemoteCommunication
{
	record RemoteHostAndSocket(String host, String mainSocket, String commSocket) {}
	
	private static final String HPCSERVER_LOCATION = "/libexec/hpcserver/hpcserver.sh";
		
	private ISecuredConnection.ISessionRemoteSocket serverMainSession;
	private ISecuredConnection.ISessionRemoteSocket serverCommSession;
	
	private SecuredConnectionSSH connectionSSH;
	private String remoteHostIP;
	
	private IConnection connection;
	
	private String errorMessage;

	/**
	 * dbManager is the main communication channel to the server: 
	 * get the remote directory, open a database and shutdown the server
	 */
	private DbManagerClient dbManager;
	
	@Override
	public String getRemoteHost() {
		if (remoteHostIP != null)
			return remoteHostIP;
		
		if (connection != null)
			return connection.getHost();
		
		return "";
	}
	
	
	@Override
	public String getRemoteHostname() {
		if (connection == null)
			return "unknown";
		
		return connection.getHost();
	}
	
	
	@Override
	public String getUsername() {
		return connection.getUsername();
	}
	
	
	
	@Override
	public void disconnect(Shell shell) throws IOException {
		if (!isConnected())
			throw new NotYetConnectedException();
		
		try {
			dbManager.shutdownRemoteServer();
		} catch (InterruptedException e) {
		    /* Clean up whatever needs to be handled before interrupting  */
		    Thread.currentThread().interrupt();
		}
		serverMainSession.disconnect();
		
		if (serverCommSession != null)
			serverCommSession.disconnect();
	}
	
	/*****
	 * Connect to a remote host
	 * @param shell
	 * @param connection
	 * @return
	 * @throws IOException
	 */
	@Override
	public ConnectionStatus connect(Shell shell, IConnection connectionInfo) throws IOException {
		if (connectionSSH != null)
			return ConnectionStatus.CONNECTED;

		// New connection:
		// - launching hpcserver on the remote host
		// - create SSH tunnel to communicate with hpcserver
		//
		connectionSSH = new SecuredConnectionSSH(shell);

		if (!connectionSSH.connect(connectionInfo))
			return ConnectionStatus.ERROR;
		
		connectionSSH.addErrorMessageHandler( message -> {
			LoggerFactory.getLogger(getClass()).error(message);
			errorMessage = message;
		});
		String command = connectionInfo.getInstallationDirectory() +  HPCSERVER_LOCATION;
		
		var remoteSession = connectionSSH.executeRemoteCommand(command);
		if (remoteSession == null) 
			return ConnectionStatus.ERROR;
		
		var remoteConnectionRecord = handleOutputAndSetConfiguration(remoteSession);
		if (remoteConnectionRecord == null)
			return ConnectionStatus.ERROR;
	
		//
		// create the SSH tunnel to communicate securely with the remote host
		//
		serverMainSession = connectionSSH.socketForwarding(remoteConnectionRecord.mainSocket);
		if (serverMainSession == null)
			return ConnectionStatus.ERROR;

		// Try to create a channel for communication tunnel session
		// this tunnel will be used to notify if the client shuts down suddenly
		if (remoteConnectionRecord.commSocket != null) {
			serverCommSession = connectionSSH.socketForwarding(remoteConnectionRecord.commSocket);
			if (serverCommSession == null)
				LoggerFactory.getLogger(getClass()).warn("The communication tunnel fails");
		}

		remoteHostIP  = remoteConnectionRecord.host;
		connection    = connectionInfo;
		var localAddr = InetAddress.getByName("localhost");

		dbManager = new DbManagerClientJavaNetHttp(localAddr, serverMainSession.getLocalPort());

		ICollectionOfConnections.putShellSession(this);
		
		return ConnectionStatus.CONNECTED;
	}
	
	
	@Override
	public String getStandardErrorMessage() {
		return errorMessage;
	}
	
	
	@Override
	public String selectDatabase(Shell shell) {
		if (serverMainSession == null)
			throw new NotYetConnectedException();
		
		var dialog = new RemoteDatabaseDialog(shell, this);
		if (dialog.open() == Window.OK) {
			return dialog.getSelectedDirectory();
		}		
		return null;
	}
	

	@Override
	public IRemoteDatabaseConnection openDatabaseConnection(Shell shell, String database) 
			throws IOException {
		if (database == null)
			return null;
		
		if (!isConnected())
			throw new NotYetConnectedException();
		
		try {
			var remoteFile = dbManager.openDatabase(database);
			if (remoteFile == null)
				return null;
			
			var socket = remoteFile.getAbsolutePathOnRemoteFilesystem();
			if (socket == null || socket.isEmpty())
				return null;
			
			return createTunnelAndBrokerClient(socket);
			
		} catch (InterruptedException e) {
		    /* Clean up whatever needs to be handled before interrupting  */
		    Thread.currentThread().interrupt();
		}
		return null;
	}
	
	
	@Override
	public RemoteDirectory getContentRemoteDirectory(String directory) 
			throws IOException, InterruptedException, DirectoryContentsNotAvailableException {
		if (!isConnected())
			throw new NotYetConnectedException();
		
		return dbManager.getDirectoryContents(directory);
	}

	
	public IConnection getConnection() {
		return connection;
	}
	
	
	private boolean isConnected() {
		return connection != null && dbManager != null && serverMainSession != null;
	}
	
	
	private IRemoteDatabaseConnection createTunnelAndBrokerClient(String brokerSocket) {		
		if (connectionSSH == null)
			return null;
		
		var brokerSession = connectionSSH.socketForwarding(brokerSocket);
		if (brokerSession == null)
			return null;
		
		return new IRemoteDatabaseConnection() {
			
			@Override
			public BrokerClient getHpcClient() {
				try {
					InetAddress localHost = InetAddress.getByName("localhost");
					return new BrokerClientJavaNetHttp(localHost, brokerSession.getLocalPort());
				} catch (UnknownHostException e) {
					throw new NotYetConnectedException();
				}
			}
			
			@Override
			public ISecuredConnection getConnection() {
				return connectionSSH;
			}
			
			@Override
			public ISecuredConnection.ISessionRemoteSocket getRemoteSocket() {
				return brokerSession;
			}
		};
	}

	
	private RemoteHostAndSocket handleOutputAndSetConfiguration(ISecuredConnection.ISessionRemote session ) 
			throws IOException {

		int maxAttempt = 10;

		String remoteIP = "";
		String remoteSocket = "";

		// handle the output from the server.
		// Sometimes the server outputs rubbish like debugging output which we don't care.
		// Skip all rubbish outputs, and just grab what we need
		
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
					return null;
				
				continue;
			}
			var response = getServerResponseInit(output);
			if (response == null ||
				response.getResponseType() != IServerResponse.ServerResponseType.SUCCESS)
				continue;
						
			if (response instanceof IServerConnectionConfig responseConfig) {
				remoteIP = responseConfig.getHost();
				remoteSocket = responseConfig.getMainSocket();
				var commSocket = responseConfig.getCommSocket();

				return new RemoteHostAndSocket(remoteIP, remoteSocket, commSocket);
			}
		}
		return null;
	}	

	
	private IServerResponse getServerResponseInit(String[] messageFromServer) {
		// looking for json message
		// sometimes the server outputs rubbish
		int i=0;
		for(; i<messageFromServer.length; i++) {
			// looking for the start of JSON message (prefixed with "{")
			if (messageFromServer[i].trim().startsWith("{"))
				break;
		}
		StringBuilder message = new StringBuilder();

		for(; i<messageFromServer.length; i++) {
			message.append(messageFromServer[i]);
		}
		// in case the output from the server is not in JSON format 
		if (message.isEmpty())
			return () -> ServerResponseType.INVALID;
		
		try {
			JsonElement mapper = JsonParser.parseString(message.toString());
			var data = mapper.getAsJsonObject();
			
			if (!data.has("status"))
				throw new JsonParseException("'status' field does not exist");
			
			var status = data.get("status").getAsString();
			
			if (!status.equalsIgnoreCase("success")) {
				return () -> ServerResponseType.ERROR;
			}
			
			if (!data.has("sock") || !data.has("host") || !data.has("comm")) {
				throw new JsonParseException("Invalid server response: " + data.toString());				
			}
			
			return new IServerConnectionConfig() {
				
				@Override
				public ServerResponseType getResponseType() {
					return ServerResponseType.SUCCESS;
				}
				
				@Override
				public String getMainSocket() {
					return data.get("sock").getAsString();
				}
				
				@Override
				public String getHost() {
					return data.get("host").getAsString();
				}
				
				@Override
				public String getCommSocket() {
					return data.get("comm").getAsString();
				}
			};

		} catch (JsonParseException e) {
			LoggerFactory.getLogger(getClass()).error("Error parsing server response init", e);
			return () -> ServerResponseType.ERROR;
		}
	}
}
