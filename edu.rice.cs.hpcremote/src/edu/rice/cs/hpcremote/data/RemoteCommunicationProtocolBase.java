// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

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
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcremote.ICollectionOfConnections;
import edu.rice.cs.hpcremote.IConnection;
import edu.rice.cs.hpcremote.ISecuredConnection;
import edu.rice.cs.hpcremote.tunnel.IRemoteCommunication;
import edu.rice.cs.hpcremote.tunnel.SecuredConnectionSSH;
import edu.rice.cs.hpcremote.ui.RemoteDatabaseDialog;

public class RemoteCommunicationProtocolBase 
	implements IRemoteCommunication
{
	/**
	 * Types of server responses:
	 * <ul>
	 *   <li>SUCCESS : everything works fine
	 *   <li>ERROR : something doesn't work right. Need to abandon the process.
	 *   <li>INVALID: something strange happens, perhaps empty string. Need to continue the process carefully.
	 * </ul>
	 */
	enum ServerResponseType {SUCCESS, ERROR, INVALID}
	
	record RemoteHostAndSocket(String host, String socket) {}
	
	private static final String HPCSERVER_LOCATION = "/libexec/hpcserver/hpcserver.sh";
		
	private ISecuredConnection.ISessionRemoteSocket serverMainSession;
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
		serverMainSession = connectionSSH.socketForwarding(remoteConnectionRecord.socket);
		if (serverMainSession == null)
			return ConnectionStatus.ERROR;
		
		remoteHostIP  = remoteConnectionRecord.host;
		connection    = connectionInfo;
		var localAddr = InetAddress.getByName("localhost");

		dbManager = new DbManagerClientJavaNetHttp(localAddr, serverMainSession.getLocalPort());

		ICollectionOfConnections.putShellSession(shell, this);
		
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
			
			return createTunnelAndRequestDatabase(socket);
			
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
	
	
	IRemoteDatabaseConnection createTunnelAndRequestDatabase(String brokerSocket) {		
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
					BrokerClient dbManagerClient = new BrokerClientJavaNetHttp(localHost, brokerSession.getLocalPort());
					return dbManagerClient;
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
			if (response == null || response.getResponseType() != ServerResponseType.SUCCESS)
				return null;
			
			remoteIP = response.getHost();
			remoteSocket = response.getSocket();
		}
		return new RemoteHostAndSocket(remoteIP, remoteSocket);
	}

	
	public IConnection getConnection() {
		return connection;
	}
	
	
	private boolean isConnected() {
		return connection != null && dbManager != null && serverMainSession != null;
	}
	
	public interface ServerResponseConnectionInit extends ServerResponse
	{
		String getSocket();
		
		String getHost();
	}
	
	public ServerResponseConnectionInit getServerResponseInit(String[] messageFromServer) {
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
		if (message.isEmpty())
			return new ServerResponseConnectionInit() {
				
				@Override
				public ServerResponseType getResponseType() {
					return ServerResponseType.INVALID;
				}
				
				@Override
				public String[] getResponseArgument() {
					return new String[0];
				}
				
				@Override
				public String getSocket() {
					return null;
				}
				
				@Override
				public String getHost() {
					return null;
				}
			};
		
		JSONObject json = new JSONObject(message.toString());
		
		if (isSuccess(json)) {
			var remoteIp = json.getString("host");
			var socket = json.getString("sock");
			
			return new ServerResponseConnectionInit() {
				
				@Override
				public ServerResponseType getResponseType() {
					return ServerResponseType.SUCCESS;
				}
				
				@Override
				public String[] getResponseArgument() {
					return new String[0];
				}
				
				@Override
				public String getSocket() {
					return socket;
				}
				
				@Override
				public String getHost() {
					return remoteIp;
				}
			};
		}		
		return null;
	}
	
	
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
	
	
	private boolean isSuccess(JSONObject json) {
		var status = json.getString("status");
		if (status == null)
			return false;
		
		return status.equalsIgnoreCase("success");
	}
}
