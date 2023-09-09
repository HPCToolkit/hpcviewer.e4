package edu.rice.cs.hpcremote.data;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;
import org.hpctoolkit.hpcclient.v1_0.HpcClientJavaNetHttp;

import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcremote.IRemoteDatabase;
import edu.rice.cs.hpcremote.ui.ConnectionDialog;

public class RemoteDatabase implements IRemoteDatabase 
{
	private String host;
	private int port;
	
	private String username;
	
	private HpcClient client;
	
	private IExperiment experiment;
	
	private DatabaseStatus status = DatabaseStatus.NOT_INITIALIZED;

	@Override
	public String getId() {
		StringBuilder sb = new StringBuilder();
		if (username != null && !username.isEmpty()) {
			sb.append(username);
			sb.append("@");
		}
		if (host != null)
			sb.append(host);
		if (port > 1) {
			sb.append(':');
			sb.append(port);
		}
		
		return sb.toString();
	}

	@Override
	public HpcClient getClient() {
		return client;
	}


	@Override
	public DatabaseStatus open(Shell shell) {
		do {
			var dialog = new ConnectionDialog(shell);
			
			if (dialog.open() == Window.CANCEL)
				return DatabaseStatus.CANCEL;
			
			host = dialog.getHost();
			port = dialog.getPort();
			
			try {
				var address = InetAddress.getByName(host);
				client = new HpcClientJavaNetHttp( address, port);
				
			} catch (UnknownHostException e) {
				MessageDialog.openError(shell, "Fail to connect", "Unable to connect to " + getId());
				status = DatabaseStatus.UNKNOWN_ERROR;
				return status;
			}
			
			RemoteDatabaseParser parser = new RemoteDatabaseParser();
			
			try {
				parser.parse(client);
				experiment = parser.getExperiment();
				
				if (experiment != null) {
					status = DatabaseStatus.OK;
					return status;
				}
	
			} catch (IOException e) {
				MessageDialog.openError(shell, 
						"Error connecting", 
						"Error message: " + e.getLocalizedMessage());
			} catch (InterruptedException e) {
			    // Restore interrupted state...
			    Thread.currentThread().interrupt();
			} catch (Exception e) {
				MessageDialog.openError(
						shell, 
						"Unknown error",  
						e.getClass().getCanonicalName() + ": " + e.getMessage());
			}
		} while (true);
	}

	
	@Override
	public IExperiment getExperimentObject() {
		return experiment;
	}


	@Override
	public void close() {
		if (experiment != null)
			experiment.dispose();
		
		// need to tell hpcserver to clean up
	}

	@Override
	public DatabaseStatus getStatus() {
		// TODO Auto-generated method stub
		return null;
	}
}
