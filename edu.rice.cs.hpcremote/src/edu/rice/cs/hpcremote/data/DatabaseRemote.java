package edu.rice.cs.hpcremote.data;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.hpctoolkit.client_server_common.profiled_source.ProfiledSourceFileId;
import org.hpctoolkit.client_server_common.profiled_source.UnknownProfiledSourceFileId;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;
import org.hpctoolkit.hpcclient.v1_0.HpcClientJavaNetHttp;

import edu.rice.cs.hpcbase.ITraceManager;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.InvalExperimentException;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.source.MetaFileSystemSourceFile;
import edu.rice.cs.hpcdata.experiment.source.SourceFile;
import edu.rice.cs.hpcremote.IDatabaseRemote;
import edu.rice.cs.hpcremote.ui.ConnectionDialog;



public class DatabaseRemote implements IDatabaseRemote
{
	private String host;
	private int port;
	
	private String username;
	
	private HpcClient client;
	
	private Experiment experiment;
	
	private DatabaseStatus status = DatabaseStatus.NOT_INITIALIZED;

	private ITraceManager traceManager;

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
		
		return sb.toString();
	}

	@Override
	public HpcClient getClient() {
		return client;
	}

	
	public DatabaseStatus open(Shell shell, String databaseId) {
		// not implemented yet
		return open(shell);
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
					var remoteDb = new RemoteDatabaseRepresentation(client, getId());
					experiment.setDatabaseRepresentation(remoteDb);
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
	public boolean hasTraceData() {
		try {
			return client.isTraceSampleDataAvailable();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public ITraceManager getORCreateTraceManager() throws IOException, InvalExperimentException {
		if (traceManager == null) {
			var opener = new RemoteTraceOpener(client, experiment);			
			traceManager = opener.openDBAndCreateSTDC(null);
		}
		return traceManager;
	}

	@Override
	public String getSourceFileContent(SourceFile fileId) throws IOException {
		if (fileId == null)
			return null;
		
		try {
			return client.getProfiledSource(ProfiledSourceFileId.make(fileId.getFileID()));
		} catch (InterruptedException e) {
		    Thread.currentThread().interrupt();
		} catch (UnknownProfiledSourceFileId e2) {
			throw new IllegalArgumentException(e2.getMessage());
		}
		return null;
	}

	@Override
	public boolean isSourceFileAvailable(Scope scope) {
		var sourceFile = scope.getSourceFile();
		if (sourceFile instanceof MetaFileSystemSourceFile) {
			MetaFileSystemSourceFile metaFile = (MetaFileSystemSourceFile) sourceFile;
			return metaFile.isCopied();
		}
		return false;
	}
}
