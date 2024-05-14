package edu.rice.cs.hpcremote.data;

import java.io.IOException;
import java.util.Iterator;

import org.eclipse.swt.widgets.Shell;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.rice.cs.hpcremote.ISecuredConnection;
import edu.rice.cs.hpcremote.data.IRemoteDirectoryContent.IFileContent;



/****
 * 
 * JSON-based main server communication protocol implementation
 * <pre>
 * request:
 * - "command": ("list", "data", "quit")
 * - "arg": string
 * 
 * server info:
 * - "status": [success, error, invalid]
 * - "host": string
 * - "sock": string
 * 
 * directory info:
 * - "status": [success, error, invalid]
 * - "message": string
 * - "path": string
 * - "contents": Array
 *    {
 *      - "name": string
 *      - "type": [d, f, h]
 *      - "time": string
 *      - "size": long
 *    }
 *    ...
 *    
 * broker info:
 * - "status": [success, error, invalid]
 * - "socket": string
 * 
 * error:
 * - "status": error
 * - "message": string
 * </pre>
 *
 */
public class RemoteCommunicationJsonProtocol extends RemoteCommunicationProtocolBase
{
	private static final String KEY_STATUS   = "status";
	private static final String KEY_COMMAND  = "command";
	private static final String KEY_ARGUMENT = "arg";
	
	@Override
	public void disconnect(
			ISecuredConnection.ISessionRemoteSocket serverMainSession, 
			Shell shell) throws IOException {

		JSONObject json = new JSONObject();
		json.put(KEY_COMMAND, "quit");		
		serverMainSession.write(json.toString());
	}


	@Override
	public ServerResponse sendCommandToOpenDatabaseAndWaitForReply(
			ISecuredConnection.ISessionRemoteSocket serverMainSession, 
			String database) throws IOException {
		
		JSONObject json = new JSONObject();
		json.put(KEY_COMMAND, "data");		
		json.put(KEY_ARGUMENT, database);		
		serverMainSession.write(json.toString());

		var reply = serverMainSession.read();
		if (reply != null && reply.length > 0) {
			var buffer = convertFromArrayToString(reply);
			final var jsonReply = new JSONObject(buffer);
			
			if (isSuccess(jsonReply)) {
				String brokerSocket = jsonReply.getString("socket");
				return new ServerResponse() {

					@Override
					public ServerResponseType getResponseType() {
						return ServerResponseType.SUCCESS;
					}

					@Override
					public String[] getResponseArgument() {
						return new String[] {brokerSocket};
					}
				};
			}
			return new ServerResponse() {

				@Override
				public ServerResponseType getResponseType() {
					return ServerResponseType.ERROR;
				}

				@Override
				public String[] getResponseArgument() {
					return new String[] {jsonReply.getString("message")};
				}
			};
		}
		return new ServerResponse() {

			@Override
			public ServerResponseType getResponseType() {
				return ServerResponseType.ERROR;
			}

			@Override
			public String[] getResponseArgument() {
				return new String[] {"No reply from the server"};
			}
		};
	}
	
	
	private boolean isSuccess(JSONObject json) {
		var status = json.getString(KEY_STATUS);
		if (status == null)
			return false;
		
		return status.equalsIgnoreCase("success");
	}

	
	@Override
	public ServerResponse sendCommandToGetDirectoryContent(
			ISecuredConnection.ISessionRemoteSocket serverMainSession, 
			String directory) throws IOException {
		
		JSONObject json = new JSONObject();
		json.put(KEY_COMMAND, "list");
		json.put(KEY_ARGUMENT, directory);
		
		serverMainSession.write(json.toString());
		
		var response = serverMainSession.read();
		if (response == null || response.length == 0)
			throw new UnknownError("Empty respose from the server");
		
		var buffer = convertFromArrayToString(response);
		
		return new ServerResponse() {

			@Override
			public ServerResponseType getResponseType() {
				var status = new JSONObject(buffer).getString(KEY_STATUS);
				
				return switch(status) {
				case "success" -> ServerResponseType.SUCCESS;
				case "error"   -> ServerResponseType.ERROR;
				default -> ServerResponseType.INVALID;
				};
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
		var buffer = convertFromArrayToString(responseFromServer);
		var json = new JSONObject(buffer);
		
		JSONArray array = (JSONArray) json.get("contents");
		if (array == null)
			return null;
		
		IFileContent []contents = new IFileContent[array.length()];
		
		int i = 0;
		Iterator<Object> iterator = array.iterator();
		
		while(iterator.hasNext()) {
			JSONObject elem = (JSONObject) iterator.next();
			contents[i] = createFileContent(elem);
			
			i++;
		}
		final String currentPathDir = json.has("path") ? json.getString("path") : "";
		
		return new IRemoteDirectoryContent() {
			
			@Override
			public String getDirectory() {
				return currentPathDir;
			}
			
			@Override
			public IFileContent[] getContent() {
				return contents;
			}
		};
	}
	
	
	private IFileContent createFileContent(JSONObject elem) {
		String type = elem.getString("type");
		String suffix = type.equals("f") ? "" : "/";			
		String name = elem.getString("name") + suffix;
		
		return new IFileContent() {
			
			@Override
			public boolean isDirectory() {
				return type.equals("d");
			}
			
			@Override
			public boolean isDatabase() {
				return type.equals("h");
			}
			
			@Override
			public String getName() {
				return name;
			}
		};
	}
	
	
	private String convertFromArrayToString(String []array) {
		var buffer = new StringBuilder();
		for(var r: array) {
			// the sign '@' is an annotation in the message
			// for example, the server can send
			//  @EOM
			// which mean the end of message.
			if (!r.startsWith("@"))
				buffer.append(r);
		}
		return buffer.toString();
	}


	@Override
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
					return null;
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
}
