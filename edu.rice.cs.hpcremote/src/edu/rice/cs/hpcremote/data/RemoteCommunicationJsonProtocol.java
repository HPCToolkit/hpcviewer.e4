package edu.rice.cs.hpcremote.data;

import java.io.IOException;
import java.util.Iterator;

import org.eclipse.swt.widgets.Shell;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.rice.cs.hpcremote.ISecuredConnection;



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
	private static final String KEY_STATUS = "status";
	
	@Override
	public void disconnect(
			ISecuredConnection.ISessionRemoteSocket serverMainSession, 
			Shell shell) throws IOException {

		JSONObject json = new JSONObject();
		json.put("command", "quit");		
		serverMainSession.write(json.toString());
	}


	@Override
	public ServerResponse sendCommandToOpenDatabaseAndWaitForReply(
			ISecuredConnection.ISessionRemoteSocket serverMainSession, 
			String database) throws IOException {
		
		JSONObject json = new JSONObject();
		json.put("data", database);		
		serverMainSession.write(json.toString());

		var reply = serverMainSession.read();
		var buffer = convertFromArrayToString(reply);
		final var jsonReply = new JSONObject(buffer);
		
		if (isSuccess(jsonReply)) {
			String brokerSocket = (String) jsonReply.get("socket");
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
	
	
	private boolean isSuccess(JSONObject json) {
		var status = json.get(KEY_STATUS);
		if (status instanceof String strStatus) {
			return strStatus.equalsIgnoreCase("success");
		}
		return false;
	}

	
	@Override
	public ServerResponse sendCommandToGetDirectoryContent(
			ISecuredConnection.ISessionRemoteSocket serverMainSession, 
			String directory) throws IOException {
		
		JSONObject json = new JSONObject();
		json.put("command", "list");
		json.put("arg", "");
		
		serverMainSession.write(json.toString());
		
		System.out.println("WAITING.....");
		
		var response = serverMainSession.read();
		if (response == null || response.length == 0)
			throw new UnknownError("Empty respose from the server");
		
		var buffer = convertFromArrayToString(response);
		
		return new ServerResponse() {

			@Override
			public ServerResponseType getResponseType() {
				var status = new JSONObject(buffer).getString("status");
				
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
		
		String []contents = new String[array.length()];
		
		int i = 0;
		Iterator<Object> iterator = array.iterator();
		
		while(iterator.hasNext()) {
			JSONObject elem = (JSONObject) iterator.next();
			contents[i] = elem.getString("name");
			i++;
		}
		return new IRemoteDirectoryContent() {
			
			@Override
			public String getDirectory() {
				return json.getString("path");
			}
			
			@Override
			public String[] getContent() {
				return contents;
			}
		};
	}
	
	
	private String convertFromArrayToString(String []array) {
		var buffer = new StringBuilder();
		for(var r: array) {
			buffer.append(r);
		}
		return buffer.toString();
	}


	@Override
	public ServerResponseConnectionInit getServerResponseInit(String[] messageFromServer) {
		// looking for json message
		// sometimes the server outputs rubbish
		String message = "";
		for(var m: messageFromServer) {
			if (m.startsWith("{") && m.endsWith("}")) {
				message = m;
				break;
			}
		}

		JSONObject json = new JSONObject(message);
		
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
