// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote;

import java.io.IOException;
import java.util.Map;

import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcremote.data.RemoteCommunicationProtocolBase;


/*****
 * Interface to manage a collection of remote connections.
 * <br/>
 * This will handle all connections from all hpcviewer instances or windows.
 */
public interface ICollectionOfConnections 
{
	ConcurrentHashMap<String, RemoteCommunicationProtocolBase> map = ConcurrentHashMap.newMap();


	/***
	 * Disconnect all opened connections
	 * 
	 * @param shell
	 * 			Current active shell
	 */
	static void disconnectAll(Shell shell) {
		var setOfSessions = getAllCommunicationSessions();
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
	static Map<String, RemoteCommunicationProtocolBase> getAllCommunicationSessions() {
		return map;
	}
	
	
	/***
	 * Get the remote connection of a given shell and connection info
	 * 
	 * @param connection
	 * @return
	 */
	static RemoteCommunicationProtocolBase getRemoteConnection(IConnection connection) {

		// check if we already have exactly the same connection as the 
		// requested host, user id and installation
		
		var setOfConnections = ICollectionOfConnections.getAllCommunicationSessions();
		if (setOfConnections.containsKey(connection.getId())) {
			return setOfConnections.get(connection.getId());			
		}
		return new RemoteCommunicationProtocolBase();
	}
	
	
	/***
	 * Store the new remote communication session.
	 * If the remote connection already exists, it will be replaced.
	 * 
	 * @param commConnection
	 * 			The new remote communication
	 * 
	 * @return the old connection if already exist
	 */
	static RemoteCommunicationProtocolBase putShellSession(RemoteCommunicationProtocolBase commConnection) {
		var setOfSessions = getAllCommunicationSessions();
		var id = commConnection.getConnection().getId();
		var oldConnection = setOfSessions.get(id);
		
		assert oldConnection == null;
		
		setOfSessions.put(id, commConnection);
		
		return oldConnection;
	}
}
