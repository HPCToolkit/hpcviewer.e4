package edu.rice.cs.hpcremote.tunnel;

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


public class SecuredConnectionSSH implements ISecuredConnection 
{
	private final Shell shell;
	private List<Session> sessions;
	
	public SecuredConnectionSSH(Shell shell) {
		this.shell = shell;
	}

	
	@Override
	public boolean connect(IConnection connectionInfo) {
		String username = connectionInfo.getUsername();
		String hostname = connectionInfo.getHost();
		String privateKey = connectionInfo.getPrivateKey();
		
		var userInfo = new RemoteUserInfoDialog(shell, hostname);
		
		JSch jsch = new JSch();
		try {
		    var irepo = new AgentIdentityRepository(new SSHAgentConnector());
		    jsch.setIdentityRepository(irepo);

		    jsch.setKnownHosts("~/.ssh/known_hosts");

			if (privateKey != null && !privateKey.isEmpty())
				jsch.addIdentity(privateKey);
			
			var configFile = connectionInfo.getConfig();
			var port = 22;

			if (configFile != null && Files.isReadable(Path.of(configFile))) {
		        ConfigRepository configRepository = OpenSSHConfig.parseFile(configFile);
		        jsch.setConfigRepository(configRepository);
		        
		        var listOfProxies = new ArrayList<Config>();
		        
		        // check if the hostname is in the config
		        var hostConfig = configRepository.getConfig(hostname);
		        while (hostConfig != null) {
		        	listOfProxies.add(hostConfig);

			        String proxy = hostConfig.getValue("ProxyJump");
		        	if (proxy != null) {
			        	hostConfig = configRepository.getConfig(proxy);
			        } else {
			        	break;
			        }
		        }
		        if (!listOfProxies.isEmpty()) {
			        Collections.reverse(listOfProxies);
			        
			        var configProxy = listOfProxies.remove(0);
			        var sessionProxy = jsch.getSession(username, configProxy.getHostname(), 22);
			        sessionProxy.setUserInfo(userInfo);
			        sessionProxy.connect();
			        
			        sessions = new ArrayList<>();
			        sessions.add(sessionProxy);
			        
			        for(var proxy: listOfProxies) {				        
						// may throw an exception
			        	var proxyName = proxy.getHostname();
			        	int assignedPort = sessionProxy.setPortForwardingL(0, proxyName, 22);
			        	
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
			// may throw an exception
			var session = jsch.getSession(username, hostname, port);
			
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
	
	
	private Session getSession() {
		if (sessions == null || sessions.isEmpty()) 
			throw new IllegalAccessError("Not connected. Need to call connect() first");
		
		int numSessions = sessions.size();
		return sessions.get(numSessions-1);
	}
	
	@Override
	public ISessionRemote executeRemoteCommand(String command) {
		if (sessions == null)
			throw new IllegalAccessError("Not connected. Need to call connect() first");
		
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
		if (sessions == null)
			throw new IllegalAccessError("Not connected. Need to call connect() first");

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

	
	public String getHost() {
		var session = getSession();
		if (session == null)
			throw new IllegalAccessError("Host is not connected");
		
		return session.getHost();
	}
	
	
	public String getUsername() {
		var session = getSession();
		if (session == null)
			throw new IllegalAccessError("Host is not connected");

		return session.getUserName();
	}
	
	private ISessionRemote executeCommand(ChannelExec channel, String command) throws JSchException, IOException {
		channel.setCommand(command);
		
		channel.setInputStream(null);
		
		//ByteArrayOutputStream errStream = new ByteArrayOutputStream();		
		channel.setErrStream(System.err);
	
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
}