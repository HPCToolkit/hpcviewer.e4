package edu.rice.cs.hpcremote.tunnel;

import java.io.IOException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import edu.rice.cs.hpcremote.ui.RemoteUserInfoDialog;

/***************************************************
 * 
 * Wrapper for SSH L-Tunneling with Jsch
 *
 ***************************************************/
public class LocalTunneling 
{
	private static final int TIMEOUT_DEFAULT = 10 * 1000;
	private final JSch  jsch;
	private final UserInfo userInfo;
	
	private Session session;
	private int port;
	
	private String session_id = null;
	
	public LocalTunneling(UserInfo userInfo)
	{
		port = 0;
		jsch = new JSch();
		this.userInfo = userInfo;
		
	}
	
	
	/*******
	 * Build SSH port local forwarding connection 
	 * 
	 * @param login_user : username at login host
	 * @param login_host : the login host
	 * @param remote_host : the remote host where a server listens
	 * @param remote_socket the remote UNIX socket	 
	 * 
	 * @return the assigned port
	 * 
	 * @throws JSchException
	 * @throws IOException 
	 */
	public int connect(String login_user, 
					   String login_host, 
					   String remote_host, 
					   String remote_socket)
			throws JSchException, IOException
	{
		String id = getSessionID(login_user, login_host, remote_host, remote_socket);

		if (session_id != null &&  (id.equals(session_id))) {
				return this.port;			
		}
		
		session = jsch.getSession(login_user, login_host, 22);
		session.setUserInfo(userInfo);
		
		// prepare the connection with timeout in mili-seconds

		session.connect(TIMEOUT_DEFAULT);
		
		this.port = session.setSocketForwardingL(null, 0, remote_socket, null, TIMEOUT_DEFAULT);

		session_id = id;
		
		return this.port;
	}
	
	
	private String getSessionID(String login_user, String login_host, 
			String remote_host, String remote_socket)
	{
		return login_user + "@" + login_host + ":" + remote_host + ":" + remote_socket;
	}
	
	
	/*****
	 * Retrieve the local port of this tunnel if the connection has been established.
	 * If not, it return 0;
	 * 
	 * @return {@code int}
	 * 			The reserved local port
	 */
	public int getLocalPort() {
		return port;
	}
	
	
	/***
	 * Retrieve the current session.
	 * If the connection fails, it returns null.
	 * 
	 * @return {@code Session}
	 */
	public Session getSession() {
		return session;
	}
	
	
	/*******
	 * disconnect tunneling
	 * 
	 * @throws JSchException
	 */
	public void disconnect() throws JSchException
	{
		session.delPortForwardingL(port);
		session.disconnect();
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return session_id;
	}
	
	/*******
	 * unit test 
	 * @param args
	 */
	public static void main(final String []args)
	{
		if (args.length != 4)
		{
			System.out.println("Required arguments: user login_host remote_host remote_socket");
			return;
		}
		var display = Display.getDefault();
		var shell = new Shell(display);
		
		RemoteUserInfoDialog ruiDlg = new RemoteUserInfoDialog(shell);
		LocalTunneling tunnel = new LocalTunneling( ruiDlg);
		
		try {
			int port = tunnel.connect(args[0], args[1], args[2], args[3]);
			System.out.println("Assigned port: " + port);
			
			tunnel.disconnect();
			
		} catch (JSchException | IOException e) {
			e.printStackTrace();
		}
	}
}
