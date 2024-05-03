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
import edu.rice.cs.hpcremote.ICollectionOfConnections;
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

	private IRemoteCommunicationProtocol remoteHostConnection;
	private IRemoteDatabaseConnection remoteDatabaseConnection;
	
	private RemoteDatabaseIdentification id;
	
	@Override
	public IDatabaseIdentification getId() {
		if (id == null)
			// dummy id
			id = new RemoteDatabaseIdentification("localhost", 0);
		
		return id;
	}
	

	@Override
	public HpcClient getClient() {
		return remoteDatabaseConnection.getHpcClient();
	}

	
	@Override
	public DatabaseStatus reset(Shell shell, IDatabaseIdentification databaseId) {
		return open(shell);
	}

	
	@Override
	public DatabaseStatus open(Shell shell) {
		
		var connectionDialog = new ConnectionDialog(shell);
		if (connectionDialog.open() == Window.CANCEL) {
			return DatabaseStatus.CANCEL;
		}
		remoteHostConnection = ICollectionOfConnections.getRemoteConnection(shell, connectionDialog);

		try {
			var connectStatus = remoteHostConnection.connect(shell, connectionDialog);
			if (connectStatus == IRemoteCommunicationProtocol.ConnectionStatus.CONNECTED) {
				var database = remoteHostConnection.selectDatabase(shell);
				if (database == null) {
					status = DatabaseStatus.CANCEL;
					return status;
				}
				remoteDatabaseConnection = remoteHostConnection.openDatabaseConnection(shell, database);
				if (remoteDatabaseConnection == null)
					return DatabaseStatus.CANCEL;
				
				if (!checkServerReadiness(remoteDatabaseConnection.getHpcClient())) {
					errorMessage = "Server is not responsive";
					status = DatabaseStatus.NOT_RESPONSIVE;
					return status;
				}
					
				id = new RemoteDatabaseIdentification(remoteHostConnection.getRemoteHost(), 0, database, remoteHostConnection.getUsername());

				experiment = openDatabase(remoteDatabaseConnection.getHpcClient(), id);
				if (experiment != null) {
					status = DatabaseStatus.OK;
					return status;
				}
				errorMessage = "Fail to access the database: " + database;
				
			} else if (connectStatus == IRemoteCommunicationProtocol.ConnectionStatus.NOT_CONNECTED) {
				status = DatabaseStatus.CANCEL;
				return status;
			}
			errorMessage = remoteHostConnection.getStandardErrorMessage();
		} catch (IOException e) {
			errorMessage = e.getLocalizedMessage();
		}
		status = DatabaseStatus.INVALID;
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
					throw new IllegalStateException(e);
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
	public String getErrorMessage() {
		return errorMessage;
	}
}
