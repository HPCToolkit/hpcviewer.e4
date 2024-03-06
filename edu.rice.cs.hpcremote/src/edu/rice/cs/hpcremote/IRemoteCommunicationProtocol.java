package edu.rice.cs.hpcremote;

import java.io.IOException;

import org.eclipse.swt.widgets.Shell;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;


/***********
 * 
 * Interface to communicate with the remote host
 * 
 ***********/
public interface IRemoteCommunicationProtocol 
{
	/**
	 * Status of the remote connection: successful, fails, or not done
	 */
	enum ConnectionStatus {NOT_CONNECTED, CONNECTED, ERROR}
	
	
	/***
	 * If the connection is successful, it return the full name or the IP address of the
	 * remote host.
	 * It returns {@code null} otherwise.
	 * 
	 * @return {@code String} the IP address of the remote host
	 */
	String getRemoteHost();
	
	
	/***
	 * The user name used to connect to the remote host.
	 * 
	 * @return {@code String}
	 */
	String getUsername();
	
	
	/****
	 * Log on to the remote host and create a secured SSH tunnel to communicate 
	 * @param shell
	 * @return
	 * @throws IOException
	 */
	ConnectionStatus connect(Shell shell) throws IOException;
	
	
	/*****
	 * Browse the remote host to get the database of interest
	 * 
	 * @param shell
	 * 
	 * @return {@code String} 
	 * 			The remote directory if selected, {@code null} otherwise
	 */
	String selectDatabase(Shell shell);
	
	
	/****
	 * Open the given remote database
	 * 
	 * @param shell
	 * 
	 * @param database
	 * 				The remote directory
	 * 
	 * @return {@code Experiment} 
	 * 			the remote database if the opening is successful, {@code null} otherwise
	 * 
	 * @throws IOException
	 */
	HpcClient openDatabaseConnection(Shell shell, String database) throws IOException;


	/****
	 * Disconnect all sessions associated with the given shell
	 * 
	 * @param shell
	 * @throws IOException
	 */
	void disconnect(Shell shell) throws IOException;
}
