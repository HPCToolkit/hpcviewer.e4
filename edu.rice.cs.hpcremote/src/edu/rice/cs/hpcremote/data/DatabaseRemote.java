package edu.rice.cs.hpcremote.data;

import java.io.IOException;
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
import edu.rice.cs.hpcremote.IDatabaseRemote;
import edu.rice.cs.hpcremote.IRemoteCommunicationProtocol;
import edu.rice.cs.hpcremote.RemoteDatabaseIdentification;
import edu.rice.cs.hpcremote.trace.RemoteTraceOpener;
import edu.rice.cs.hpcremote.tunnel.SecuredConnectionSSH;



public class DatabaseRemote implements IDatabaseRemote
{
	private HpcClient client;
	
	private Experiment experiment;
	
	private DatabaseStatus status = DatabaseStatus.NOT_INITIALIZED;
	private String errorMessage = "";
	
	private ITraceManager traceManager;

	private SecuredConnectionSSH brokerSSH;
	
	private RemoteCommunicationProtocol remoteComm;
	
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
		return client;
	}

	
	@Override
	public DatabaseStatus reset(Shell shell, IDatabaseIdentification databaseId) {
		return open(shell);
	}
	
	
	@Override
	public DatabaseStatus open(Shell shell) {
		if (remoteComm == null) {
			remoteComm = new RemoteCommunicationProtocol();
		}
		try {
			var connectStatus = remoteComm.connect(shell);
			if (connectStatus == IRemoteCommunicationProtocol.ConnectionStatus.CONNECTED) {
				var database = remoteComm.selectDatabase(shell);
				if (database == null) {
					status = DatabaseStatus.CANCEL;
					return status;
				}
				client = remoteComm.openDatabaseConnection(shell, database);
				if (client == null)
					return DatabaseStatus.CANCEL;
				
				id = new RemoteDatabaseIdentification(remoteComm.getRemoteHost(), 0, database, remoteComm.getUsername());

				experiment = openDatabase(client, id);
				if (experiment != null) {
					status = DatabaseStatus.OK;
					return status;
				}
				errorMessage = "Fail to access the database: " + database;
				
			} else if (connectStatus == IRemoteCommunicationProtocol.ConnectionStatus.NOT_CONNECTED) {
				status = DatabaseStatus.CANCEL;
				return status;
			}
		} catch (IOException e) {
			errorMessage = "Unknown error\n" + e.getLocalizedMessage();
		}
		status = DatabaseStatus.INVALID;
		return status;
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
		// need to tell hpcserver to clean up
		if (brokerSSH != null)
			brokerSSH.close();
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


	@Override
	public String getErrorMessage() {
		return errorMessage;
	}
}
