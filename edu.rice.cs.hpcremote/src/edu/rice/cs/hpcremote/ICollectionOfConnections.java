package edu.rice.cs.hpcremote;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcremote.data.RemoteCommunicationJsonProtocol;
import edu.rice.cs.hpcremote.data.RemoteCommunicationProtocolBase;

public interface ICollectionOfConnections 
{
	static final String KEY_COMMUNICATION = "hpcviewer.comm";

	static void disconnectAll(Shell shell) {
		var setOfSessions = getShellSessions(shell);
		if (setOfSessions.isEmpty())
			return;
		
		setOfSessions.forEach( (key, val) -> {
			try {
				val.disconnect(shell);
			} catch (IOException e) {
				// nothing to do, just log
				LoggerFactory.getLogger("ICollectionOfConnections").error("Fail to disconnect", e);
			}
		});
	}
	
	/***
	 * Get the remote sessions of the given shell
	 * 
	 * @param shell
	 * 
	 * @return {@code Map} of remote sessions
	 */
	static Map<String, RemoteCommunicationProtocolBase> getShellSessions(Shell shell) {
		var setOfSessions = shell.getData(KEY_COMMUNICATION);
		if (setOfSessions instanceof Map<?, ?>) {
			return (Map<String, RemoteCommunicationProtocolBase>) setOfSessions;
		}
		return new HashMap<>();
	}
	
	
	/***
	 * Get the remote connection of a given shell and connection info
	 * 
	 * @param shell
	 * @param connection
	 * @return
	 */
	static RemoteCommunicationProtocolBase getRemoteConnection(Shell shell, IConnection connection) {

		// check if we already have exactly the same connection as the 
		// requested host, user id and installation
		
		var setOfConnections = ICollectionOfConnections.getShellSessions(shell);
		if (setOfConnections.containsKey(connection.getId())) {
			return setOfConnections.get(connection.getId());			
		}
		return new RemoteCommunicationJsonProtocol();
	}
	
	/***
	 * Store the communication session to this shell
	 * 
	 * @param shell
	 * @param id
	 * @param commConnection
	 */
	static void putShellSession(Shell shell, String id, RemoteCommunicationProtocolBase commConnection) {
		Map<String, RemoteCommunicationProtocolBase> mapOfSessions;
		var setOfSessions = shell.getData(KEY_COMMUNICATION);
		if (setOfSessions == null) {
			mapOfSessions = new HashMap<>();
		} else {
			mapOfSessions = (Map<String, RemoteCommunicationProtocolBase>) setOfSessions;
		}
		mapOfSessions.put(id, commConnection);
		shell.setData(KEY_COMMUNICATION, mapOfSessions);
	}
}
