package edu.rice.cs.hpcremote;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcremote.data.RemoteCommunicationProtocol;

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
				// nothing to do
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
	static Map<String, RemoteCommunicationProtocol> getShellSessions(Shell shell) {
		var setOfSessions = shell.getData(KEY_COMMUNICATION);
		if (setOfSessions instanceof Map<?, ?>) {
			return (Map<String, RemoteCommunicationProtocol>) setOfSessions;
		}
		return new HashMap<>();
	}
	
	
	/***
	 * Store the communication session to this shell
	 * 
	 * @param shell
	 * @param id
	 * @param commConnection
	 */
	static void putShellSession(Shell shell, String id, RemoteCommunicationProtocol commConnection) {
		Map<String, RemoteCommunicationProtocol> mapOfSessions;
		var setOfSessions = shell.getData(KEY_COMMUNICATION);
		if (setOfSessions == null) {
			mapOfSessions = new HashMap<>();
		} else {
			mapOfSessions = (Map<String, RemoteCommunicationProtocol>) setOfSessions;
		}
		mapOfSessions.put(id, commConnection);
		shell.setData(KEY_COMMUNICATION, mapOfSessions);
	}
}
