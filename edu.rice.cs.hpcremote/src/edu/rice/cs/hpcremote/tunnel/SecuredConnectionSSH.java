// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcremote.tunnel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.jcraft.jsch.AgentIdentityRepository;
import com.jcraft.jsch.AgentProxyException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ConfigRepository;
import com.jcraft.jsch.ConfigRepository.Config;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.OpenSSHConfig;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SSHAgentConnector;

import edu.rice.cs.hpcremote.IConnection;
import edu.rice.cs.hpcremote.ISecuredConnection;
import edu.rice.cs.hpcremote.ui.RemoteUserInfoDialog;


/*************************************************
 * 
 * Class to manage the connection via SSH
 * 
 *************************************************/
public class SecuredConnectionSSH implements ISecuredConnection 
{
	private static final int SSH_PORT = 22;
	
	private record ProxyConfig(String host, Config config) {}

	private final Shell shell;
	private List<Session> sessions;
	
	private IErrorMessageHandler handler;
	
	public SecuredConnectionSSH(Shell shell) {
		this.shell = shell;
	}

	
	@Override
	public boolean connect(IConnection connectionInfo) {
		String username = connectionInfo.getUsername();
		String hostname = connectionInfo.getHost();
		String privateKey = connectionInfo.getPrivateKey();
		
		var userInfo = new RemoteUserInfoDialog(shell);
		
		JSch jsch = new JSch();
		try {
		    jsch.setKnownHosts("~/.ssh/known_hosts");

			if (privateKey != null && !privateKey.isEmpty()) {
			    var irepo = new AgentIdentityRepository(new SSHAgentConnector());
			    
			    jsch.setIdentityRepository(irepo);
				jsch.addIdentity(privateKey);
			}
			
			if (canConnectUsingConfigRepo(jsch, connectionInfo, userInfo))
				return true;
			
			// may throw an exception
			var session = jsch.getSession(username, hostname, SSH_PORT);
			
			session.setUserInfo(userInfo);
			
			// this may throw an exception
			session.connect();
			
			sessions = new ArrayList<>(1);
			sessions.add(session);
			
		} catch (JSchException e) {
			MessageDialog.openError(shell, "Unabe to connect " + hostname, e.getLocalizedMessage());
			return false;
		} catch (IOException e) {
			MessageDialog.openError(shell, "I/O Error", e.getMessage());
			return false;
		} catch (AgentProxyException e) {
			MessageDialog.openError(shell, "Proxy agent error", e.getMessage());
			return false;
		}
		return true;
	}
	
	
	/****
	 * Given a connection object, try to connect to remote host using SSH Configuration Repo, usually located
	 * at `~/.ssh/config`.
	 * 
	 * The connection can be using an alias or proxy jump. If the connection is successful, it returns true.
	 * 
	 * @param jsch
	 * @param connectionInfo
	 * @param userInfo
	 * 
	 * @return {@code boolean} true if the connection succeeds, false otherwise.
	 * 
	 * @throws JSchException
	 * @throws IOException
	 */
	private boolean canConnectUsingConfigRepo(JSch jsch, IConnection  connectionInfo, RemoteUserInfoDialog userInfo) 
			throws JSchException, IOException {
		
		var configFile = connectionInfo.getConfig();

		if (configFile != null && Files.isReadable(Path.of(configFile))) {
	        ConfigRepository configRepository = OpenSSHConfig.parseFile(configFile);
	        jsch.setConfigRepository(configRepository);
	        
	        var listOfProxies = new ArrayList<ProxyConfig>();
	        var hostname = connectionInfo.getHost();
	        
	        // check if the hostname is in the config
	        var hostConfig = configRepository.getConfig(hostname);
	        while (hostConfig != null) {
	        	listOfProxies.add(new ProxyConfig(hostname, hostConfig));

	        	hostname = hostConfig.getValue("ProxyJump");
	        	if (hostname != null) {
		        	hostConfig = configRepository.getConfig(hostname);
		        } else {
		        	break;
		        }
	        }
	        if (!listOfProxies.isEmpty()) {
	        	// need to reverse the order of the list so that the first host to connect is the one 
	        	// in the last host of the config repo
	        	// 
	        	// if we have the SSH config:
	        	// Host hostA
	        	// 		HostName hostA.rice.edu
	        	// 
	        	// Host hostB
	        	//		HostName hostB.rice.edu
	        	//		ProxyJump hostA
	        	//
	        	// Then when we want to connect to hostB, the initial order is:
	        	//		hostB, hostA
	        	//
	        	// But the order of connection should be:
	        	//		hostA, hostB
	        	
		        Collections.reverse(listOfProxies);
		        
		        var configProxy = listOfProxies.remove(0);
		        
		        var username = connectionInfo.getUsername();
		        
		        String remoteHost = configProxy.config.getHostname() == null ? configProxy.host : configProxy.config.getHostname();
		        
		        var sessionProxy = jsch.getSession(username, remoteHost, SSH_PORT);
		        sessionProxy.setUserInfo(userInfo);
		        sessionProxy.connect();
		        
		        sessions = new ArrayList<>();
		        sessions.add(sessionProxy);
		        
		        for(var proxy: listOfProxies) {
					// may throw an exception
		        	var proxyName = proxy.config.getHostname() == null ? proxy.host : proxy.config.getHostname();
		        	int assignedPort = sessionProxy.setPortForwardingL(0, proxyName, SSH_PORT);
		        	
					sessionProxy = jsch.getSession(username, "127.0.0.1", assignedPort);

					Properties config = new Properties();
				    config.put("StrictHostKeyChecking", "no");
				    
				    sessionProxy.setConfig(config);
				    sessionProxy.setUserInfo(userInfo);
				    sessionProxy.setHostKeyAlias(proxyName);
				    
				    sessionProxy.connect();
				    
				    sessions.add(sessionProxy);
		        }
		        return true;
	        }
		}
		return false;
	}
	
	
	private Session getSession() {
		if (sessions == null || sessions.isEmpty()) 
			throw new IllegalAccessError("Not connected. Need to call connect() first");
		
		int numSessions = sessions.size();
		return sessions.get(numSessions-1);
	}
	
	
	@Override
	public ISessionRemote executeRemoteCommand(String command) {
		try {
			var session = getSession();
			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
			return executeCommand(channelExec, command);
			
		} catch (JSchException | IOException e) {
			MessageDialog.openError(shell, "Fail to execute remote command", e.getLocalizedMessage());
			return null;
		}
	}

	
	@Override
	public ISessionRemoteSocket socketForwarding(String socketPath) {
		try {
			var session = getSession();
			return new SocketForwardingSession(session, socketPath);

		} catch (JSchException | IOException e) {
			MessageDialog.openError(
					shell, 
					"Fail to create SSH tunnel", 
					socketPath + ": " +  e.getLocalizedMessage());
		}
		return null;
	}
	
	
	private ISessionRemote executeCommand(ChannelExec channel, String command) throws JSchException, IOException {
		channel.setCommand(command);
		
		channel.setInputStream(null);
		
		var errStream = new ByteArrayOutputStream() {
			@Override
			public void flush() throws IOException {
				// notify the caller we have new error message from the remote host
				if (handler != null) {
					String str = new String(buf);
					handler.message(str);
				}
		    }
		};
		channel.setErrStream(errStream);
	
		final var inStream = channel.getInputStream();
		
		// need to call connect to execute the command
		channel.connect();
		
		return new ISessionRemote() {
			
			@Override
			public Session getSession() throws JSchException {
				return channel.getSession();
			}
			
			@Override
			public void disconnect() {
				channel.disconnect();
			}
			
			@Override
			public InputStream getLocalInputStream() throws IOException {
				return inStream;
			}

			@Override
			public OutputStream getLocalOutputStream() throws IOException {
				return channel.getOutputStream();
			}
		};
	}


	@Override
	public void close() {
		if (sessions == null || sessions.isEmpty())
			return;

		sessions.forEach(Session::disconnect);
	}
	
	
	@Override
	public void addErrorMessageHandler(IErrorMessageHandler handler) {
		this.handler = handler;
	}
	
}