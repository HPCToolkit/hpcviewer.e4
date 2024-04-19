package edu.rice.cs.hpcremote.data;

import java.io.IOException;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcremote.ISecuredConnection;
import edu.rice.cs.hpcremote.data.IRemoteDirectoryContent.IFileContent;

public class RemoteCommunicationProtocol extends RemoteCommunicationProtocolBase
{	
	
	@Override
	public void disconnect(
			ISecuredConnection.ISessionRemoteSocket serverMainSession, 
			Shell shell) throws IOException {

		serverMainSession.write("@QUIT");
	}
	

	@Override
	public ServerResponse sendCommandToOpenDatabaseAndWaitForReply(
			ISecuredConnection.ISessionRemoteSocket serverMainSession, 
			String database) throws IOException {
		
		serverMainSession.write("@DATA " + database);
		
		var inputs = serverMainSession.read();
		if (inputs == null || inputs.length == 0)
			return ServerResponse.INVALID;
		
		var response = inputs[0];
		if (response.startsWith("@ERR")) {
			return new ServerResponse() {

				@Override
				public ServerResponseType getResponseType() {
					return ServerResponseType.ERROR;
				}

				@Override
				public String[] getResponseArgument() {
					return new String[] {response.substring(5)};
				}				
			};
		} else if (response.startsWith("@SOCK")) {
			return new ServerResponse() {

				@Override
				public ServerResponseType getResponseType() {
					return ServerResponseType.SUCCESS;
				}

				@Override
				public String[] getResponseArgument() {
					return new String[] {response.substring(6)};
				}
			};
		}
		return ServerResponse.INVALID;
	}


	@Override
	public ServerResponse sendCommandToGetDirectoryContent(
			ISecuredConnection.ISessionRemoteSocket serverMainSession, 
			String directory) throws IOException {
		
		String message = directory.isEmpty() ? "@LIST" : "@LIST " + directory;
		serverMainSession.write(message.trim());
		
		var response = serverMainSession.read();
		return new ServerResponse() {

			@Override
			public ServerResponseType getResponseType() {
				if (response[0].startsWith("@LIST"))
					return ServerResponseType.SUCCESS;
				else if (response[0].startsWith("@ERR"))
					return ServerResponseType.ERROR;
				else
					return ServerResponseType.INVALID;
			}

			@Override
			public String[] getResponseArgument() {
				return response;
			}
		};
	}


	@Override
	public IRemoteDirectoryContent createDirectoryContent(
			ISecuredConnection.ISessionRemoteSocket serverMainSession, 
			String[] responseFromServer) {
		
		var currentDir = responseFromServer[0].substring(6);
		IFileContent []content = new IFileContent[responseFromServer.length-1];
		
		for(int i=0; i<content.length; i++) {
			final int index = i + 1;
			content[i] = new IFileContent() {
				
				@Override
				public boolean isDirectory() {
					return responseFromServer[index].endsWith("/");
				}
				
				@Override
				public boolean isDatabase() {
					return isDirectory();
				}
				
				@Override
				public String getName() {
					return responseFromServer[index];
				}
			}; 
		}
		return new IRemoteDirectoryContent() {
			
			@Override
			public String getDirectory() {
				return currentDir;
			}
			
			@Override
			public IFileContent[] getContent() {
				return content;
			}
		};
	}


	@Override
	public ServerResponseConnectionInit getServerResponseInit(String[] messageFromServer) {
		String remoteIP = "";
		String remoteSocket = "";
		
		for (int i=0; i<messageFromServer.length; i++) {
			var pair = messageFromServer[i];
			if (pair.startsWith("@HOST")) {
				remoteIP = pair.substring(6); 
			} else if (pair.startsWith("@SOCK")) {
				remoteSocket = pair.substring(6);
			} else {
				LoggerFactory.getLogger(getClass()).debug("Unknown remote command: " + pair);
			}
		}
		final var socket = remoteSocket;
		final var host = remoteIP;
		
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
				return host;
			}
		};
	}
}
