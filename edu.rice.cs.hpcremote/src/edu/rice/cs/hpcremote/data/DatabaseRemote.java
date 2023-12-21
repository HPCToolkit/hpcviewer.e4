package edu.rice.cs.hpcremote.data;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.hpctoolkit.client_server_common.profiled_source.ProfiledSourceFileId;
import org.hpctoolkit.client_server_common.profiled_source.UnknownProfiledSourceFileId;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;
import org.hpctoolkit.hpcclient.v1_0.HpcClientJavaNetHttp;
import org.hpctoolkit.hpcclient.v1_0.UnknownCallingContextException;
import org.hpctoolkit.hpcclient.v1_0.UnknownProfileIdException;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcbase.IDatabaseIdentification;
import edu.rice.cs.hpcbase.ITraceManager;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.InvalExperimentException;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.source.MetaFileSystemSourceFile;
import edu.rice.cs.hpcdata.experiment.source.SourceFile;
import edu.rice.cs.hpcdata.util.IProgressReport;
import edu.rice.cs.hpcremote.IDatabaseRemote;
import edu.rice.cs.hpcremote.RemoteDatabaseIdentification;
import edu.rice.cs.hpcremote.trace.RemoteTraceOpener;
import edu.rice.cs.hpcremote.ui.ConnectionDialog;



public class DatabaseRemote implements IDatabaseRemote
{
	private RemoteDatabaseIdentification id;
	private HpcClient client;
	
	private Experiment experiment;
	
	private DatabaseStatus status = DatabaseStatus.NOT_INITIALIZED;

	private ITraceManager traceManager;

	@Override
	public IDatabaseIdentification getId() {
		if (id == null)
			// dummy id
			id = new RemoteDatabaseIdentification("localhost", 0);
		
		return id;
	}
	

	@Override
	public HpcClient getClient() {
		return client;
	}

	
	@Override
	public DatabaseStatus reset(Shell shell, IDatabaseIdentification databaseId) {
		id = (RemoteDatabaseIdentification) databaseId;
		return open(shell);
	}

	@Override
	public DatabaseStatus open(Shell shell) {
		do {
			var dialog = new ConnectionDialog(shell, id);
			
			if (dialog.open() == Window.CANCEL)
				return DatabaseStatus.CANCEL;
			
			var host = dialog.getHost();
			var port = dialog.getPort();
			
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
					var remoteDb = new RemoteDatabaseRepresentation(client, getId().id());
					experiment.setDatabaseRepresentation(remoteDb);
					id = new RemoteDatabaseIdentification(host, port, client.getDatabasePath().toString(), null);

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
	public IMetricManager getExperimentObject() {
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
			LoggerFactory.getLogger(getClass()).error("Cannot check data availability", e);
		    // Restore interrupted state...
		    Thread.currentThread().interrupt();
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
			throw new IllegalArgumentException(e2);
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


	@Override
	public RootScope createFlatTree(Scope rootCCT, RootScope rootFlat, IProgressReport progressMonitor) {
		var collectMetricsCCT = new CollectAllMetricsVisitor(progressMonitor);
		rootCCT.dfsVisitScopeTree(collectMetricsCCT);
		try {
			collectMetricsCCT.postProcess(client);			 
			return experiment.createFlatView(rootCCT, rootFlat, progressMonitor);
			
		} catch (UnknownProfileIdException | UnknownCallingContextException e) {
			// something wrong with the profile or the database or the tree.
			// Let the caller knows about this/
			throw new IllegalArgumentException(e);
		} catch (IOException e) {
			// It's either the network or the security or perhaps the file systems?
			throw new IllegalStateException("I/O Error", e);
		} catch (InterruptedException e) {
		    // Restore interrupted state...
		    Thread.currentThread().interrupt();
		}
		return null;
	}
}
