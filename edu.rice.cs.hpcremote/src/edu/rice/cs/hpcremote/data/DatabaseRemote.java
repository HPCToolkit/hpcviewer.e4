package edu.rice.cs.hpcremote.data;

import java.io.IOException;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.hpctoolkit.client_server_common.profiled_source.ProfiledSourceFileId;
import org.hpctoolkit.client_server_common.profiled_source.UnknownProfiledSourceFileId;
import org.hpctoolkit.hpcclient.v1_0.HpcClient;
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
import edu.rice.cs.hpcremote.DefaultConnection;
import edu.rice.cs.hpcremote.ICollectionOfConnections;
import edu.rice.cs.hpcremote.IConnection;
import edu.rice.cs.hpcremote.IDatabaseRemote;
import edu.rice.cs.hpcremote.IRemoteCommunicationProtocol;
import edu.rice.cs.hpcremote.RemoteDatabaseIdentification;
import edu.rice.cs.hpcremote.trace.RemoteTraceOpener;
import edu.rice.cs.hpcremote.ui.ConnectionDialog;



public class DatabaseRemote implements IDatabaseRemote
{	
	private Experiment experiment;
	
	private DatabaseStatus status = DatabaseStatus.NOT_INITIALIZED;
	private String errorMessage = "";
	
	private ITraceManager traceManager;

	private IRemoteDatabaseConnection remoteDatabaseConnection;
	
	private RemoteDatabaseIdentification id;
	
	@Override
	public IDatabaseIdentification getId() {
		if (id == null)
			throw new IllegalAccessError("Not connected or the remote database is invalid");
		return id;
	}
	

	@Override
	public HpcClient getClient() {
		return remoteDatabaseConnection.getHpcClient();
	}

	
	@Override
	public DatabaseStatus reset(Shell shell, IDatabaseIdentification databaseId) {
		RemoteDatabaseIdentification remoteId = (RemoteDatabaseIdentification) databaseId;
		
		var connectionInfo = new DefaultConnection(remoteId);
		return tryOpen(shell, connectionInfo, remoteId);
	}

	
	@Override
	public DatabaseStatus open(Shell shell) {
		var connectionDialog = new ConnectionDialog(shell);
		
		if (connectionDialog.open() == Window.CANCEL) {
			return DatabaseStatus.CANCEL;
		}
		return tryOpen(shell, connectionDialog, null);
	}

	
	private DatabaseStatus tryOpen(Shell shell, IConnection connectionInfo, RemoteDatabaseIdentification remoteId) {
		try {
			status = doOpen(shell, connectionInfo, remoteId);
		} catch (IOException e) {
			errorMessage = e.getLocalizedMessage();
			status = DatabaseStatus.INVALID;
		}
		return status;
	}
	
	
	private DatabaseStatus doOpen(Shell shell, IConnection connectionInfo, RemoteDatabaseIdentification remoteId) 
			throws IOException {
		var remoteHostConnection = ICollectionOfConnections.getRemoteConnection(shell, connectionInfo);
		var connectStatus = remoteHostConnection.connect(shell, connectionInfo);

		if (connectStatus == IRemoteCommunicationProtocol.ConnectionStatus.CONNECTED) {
			String database;
			if (remoteId == null) 
				database = remoteHostConnection.selectDatabase(shell);
			else 
				database = remoteId.getPath();

			remoteDatabaseConnection = remoteHostConnection.openDatabaseConnection(shell, database);
			if (remoteDatabaseConnection == null)
				return DatabaseStatus.CANCEL;
			
			if (!checkServerReadiness(remoteDatabaseConnection.getHpcClient())) {
				errorMessage = "Server is not responsive";
				status = DatabaseStatus.NOT_RESPONSIVE;
				return status;
			}
			if (remoteId == null) {
				id = new RemoteDatabaseIdentification(
						remoteHostConnection.getRemoteHostname(), 
						database, 
						remoteHostConnection.getUsername());
			
				id.setRemoteInstallation(connectionInfo.getInstallationDirectory());
			} else {
				id = remoteId;
			}

			experiment = openDatabase(remoteDatabaseConnection.getHpcClient(), id);
			if (experiment != null) {
				status = DatabaseStatus.OK;
				return status;
			}
			errorMessage = "Fail to access the database: " + database;
			
		} else if (connectStatus == IRemoteCommunicationProtocol.ConnectionStatus.NOT_CONNECTED) {
			status = DatabaseStatus.CANCEL;
			return status;
		} else if (connectStatus == IRemoteCommunicationProtocol.ConnectionStatus.ERROR) {
			status = DatabaseStatus.UNKNOWN_ERROR;
		}
		errorMessage = remoteHostConnection.getStandardErrorMessage();
		return status;
	}
	
	
	private boolean checkServerReadiness(HpcClient client) {
		// maximum we wait for 10 seconds max
		int numAttempt = 100;
		while(numAttempt > 0) {
			try {
				var path = client.getDatabasePath();
				if (path != null)
					return true;
			} catch (IOException e) {
				// the server may not ready
				numAttempt--;
				Thread.yield();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// nothing
				}
			} catch (InterruptedException e) {
				// nothing
			}
		}
		return false;
	}
	
	private Experiment openDatabase(HpcClient client, IDatabaseIdentification id) throws IOException {
		RemoteDatabaseParser parser = new RemoteDatabaseParser();
		try {
			parser.parse(client);
			var exp = parser.getExperiment();
			
			if (exp == null)
				return null;
			var remoteDb = new RemoteDatabaseRepresentation(client, id.id());
			exp.setDatabaseRepresentation(remoteDb);
			
			return exp;

		} catch (InterruptedException e) {
		    // Restore interrupted state...
			Thread.currentThread().interrupt();
		}
		return null;
	}
	
	@Override
	public IMetricManager getExperimentObject() {
		return experiment;
	}


	@Override
	public void close() {		
		if (remoteDatabaseConnection != null) {
			// notify the server to close this connection
			if (remoteDatabaseConnection.getHpcClient() != null) {
				try {
					remoteDatabaseConnection.getHpcClient().close();
				} catch (IOException e) {
					// The server has been closed or we have problem with the network
					LoggerFactory.getLogger(getClass()).error("Fail to close the server", e);
					return;
				} catch (InterruptedException e) {
				    Thread.currentThread().interrupt();
				}
			}
			// close the socket
			remoteDatabaseConnection.getRemoteSocket().disconnect();
		}
	}

	@Override
	public DatabaseStatus getStatus() {
		return status;
	}

	@Override
	public boolean hasTraceData() {
		try {
			return remoteDatabaseConnection.getHpcClient().isTraceSampleDataAvailable();
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
			var opener = new RemoteTraceOpener(remoteDatabaseConnection.getHpcClient(), experiment);			
			traceManager = opener.openDBAndCreateSTDC(null);
		}
		return traceManager;
	}

	@Override
	public String getSourceFileContent(SourceFile fileId) throws IOException {
		if (fileId == null)
			return null;
		
		try {
			return remoteDatabaseConnection.getHpcClient().getProfiledSource(ProfiledSourceFileId.make(fileId.getFileID()));
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
		if (sourceFile instanceof MetaFileSystemSourceFile metaFile) {
			return metaFile.isCopied();
		}
		return false;
	}


	@Override
	public RootScope createFlatTree(Scope rootCCT, RootScope rootFlat, IProgressReport progressMonitor) {
		var collectMetricsCCT = new CollectAllMetricsVisitor(progressMonitor);
		rootCCT.dfsVisitScopeTree(collectMetricsCCT);
		try {
			collectMetricsCCT.postProcess(remoteDatabaseConnection.getHpcClient());			 
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


	@Override
	public RootScope createCallersView(Scope rootCCT, RootScope rootBottomUp, IProgressReport progress) {
		var collectMetricVisitor = new CollectBottomUpMetricsVisitor(progress);
		rootCCT.dfsVisitScopeTree(collectMetricVisitor);
		
		try {
			collectMetricVisitor.postProcess(getClient());			
			return experiment.createCallersView(rootCCT, rootBottomUp, progress);
			
		} catch (UnknownProfileIdException | UnknownCallingContextException | IOException e) {
			var message = "Fail to collect metrics from the server";
			LoggerFactory.getLogger(getClass()).error(message, e);
			throw new IllegalArgumentException(message);
			
		} catch (InterruptedException e) {
		    /* Clean up whatever needs to be handled before interrupting  */
		    Thread.currentThread().interrupt();
		}
		return rootBottomUp;
	}


	@Override
	public String getErrorMessage() {
		return errorMessage;
	}
}
