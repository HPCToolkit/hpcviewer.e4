package edu.rice.cs.hpcremote.data;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;
import org.hpctoolkit.hpcclient.v1_0.HpcClientJavaNetHttp;

import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IDatabaseRepresentation;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcremote.IRemoteDatabase;
import edu.rice.cs.hpcremote.ui.ConnectionDialog;

public class RemoteDatabase implements IRemoteDatabase, IDatabaseRepresentation
{
	private String host;
	private int port;
	
	private String username;
	
	private HpcClient client;
	
	private Experiment experiment;
	
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
		sb.append('/');
		sb.append( experiment.getDirectory() );
		
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
					experiment.setDatabaseRepresentation(this);
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
		// need to tell hpcserver to clean up
	}

	@Override
	public DatabaseStatus getStatus() {
		return status;
	}

	@Override
	public File getFile() {
		return new File(".");
	}

	@Override
	public void setFile(File file) {
		throw new IllegalAccessError("Invalid access to setFile");
	}

	@Override
	public void open(IExperiment experiment) throws Exception {
		RemoteDatabaseParser parser = new RemoteDatabaseParser();
		parser.collectMetaData(client);
		
		var experimentNew = parser.getExperiment();
		var root = experimentNew.getRootScope();
		
		experiment.setRootScope(root);
		
		this.experiment = (Experiment) experiment;
	}

	@Override
	public void reopen(IExperiment experiment) throws Exception {
		open(experiment);
	}

	@Override
	public IDatabaseRepresentation duplicate() {
		var db = new RemoteDatabase();
		db.host = host;
		db.port = port;
		db.username = username;
		db.client = client;
		db.experiment = experiment;
		db.status = status;
		
		return db;
	}

	@Override
	public int getTraceDataVersion() {
		try {
			if (client.getMinimumTraceSampleTimestamp().isPresent())
				return Constants.EXPERIMENT_SPARSE_VERSION;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return -1;
	}
}
