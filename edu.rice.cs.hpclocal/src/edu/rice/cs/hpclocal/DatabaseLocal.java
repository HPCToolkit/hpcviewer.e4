package edu.rice.cs.hpclocal;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcbase.IDatabaseIdentification;
import edu.rice.cs.hpcbase.ITraceManager;
import edu.rice.cs.hpcbase.ProgressReport;
import edu.rice.cs.hpcbase.map.ProcedureAliasMap;
import edu.rice.cs.hpcdata.db.DatabaseManager;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.InvalExperimentException;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.source.EmptySourceFile;
import edu.rice.cs.hpcdata.experiment.source.FileSystemSourceFile;
import edu.rice.cs.hpcdata.experiment.source.SourceFile;
import edu.rice.cs.hpcdata.util.IProgressReport;
import edu.rice.cs.hpcdata.util.Util;

public class DatabaseLocal implements IDatabaseLocal 
{
	private Experiment experiment;
	private String errorMsg;
	private DatabaseStatus status = DatabaseStatus.NOT_INITIALIZED;
	private ITraceManager traceManager;
	private LocalDatabaseIdentification databaseId;
	
	@Override
	public IDatabaseIdentification getId() {
		if (databaseId == null) {
			if (experiment != null)
				databaseId = new LocalDatabaseIdentification(experiment.getID());
			else
				databaseId = new LocalDatabaseIdentification("local:" + System.currentTimeMillis());
		}
		return databaseId;
	}
	

	@Override
	public DatabaseStatus open(Shell shell) {
		var experimentManager = new ExperimentManager();
		try {
			var filename = experimentManager.openFileExperiment(shell);
			if (filename != null && !filename.isEmpty()) {
				status = open(filename);
			} else {
				status = DatabaseStatus.CANCEL;
			}
		} catch (Exception e) {
			errorMsg = "File to open the database: " + e.getMessage();
			status = DatabaseStatus.INVALID; 
		}
		return status;
	}
	

	@Override
	public void close() {
		experiment = null;
		errorMsg = null;
	}

	@Override
	public IMetricManager getExperimentObject() {
		return experiment;
	}

	@Override
	public String getDirectory() {
		return experiment.getID();
	}

	@Override
	public DatabaseStatus reset(Shell shell, IDatabaseIdentification id) {
		status = DatabaseStatus.INVALID;
		if (id == null) {
			return status;
		}
				
		try {
			status = open(id.id());
		} catch (InterruptedException e) {
			LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
		}
		
		return status;
	}

	
	private DatabaseStatus open(String filename) throws InterruptedException {
		if (!Files.isRegularFile(Paths.get(filename)) && ExperimentManager.checkDirectory(filename)) {
			var filepath = DatabaseManager.getDatabaseFilePath(filename);
			if (filepath.isEmpty()) {
				errorMsg = filename + ": is not a HPCToolkit database";
				
				status = DatabaseStatus.INEXISTENCE;
				
				return status;				
			}
			filename = filepath.get();
		}
		var file = new File(filename);
		
		if (!file.canRead()) {
			errorMsg = filename + ": is not accessible";
			return DatabaseStatus.INVALID;
		}
		Job job = new Job("Opening the database") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				experiment = new Experiment();
				
				ProcedureAliasMap map = new ProcedureAliasMap();		
				var localDb = new LocalDatabaseRepresentation(file, map, new ProgressReport(monitor));
				
				try {
					experiment.open(localDb);
				} catch (Exception e) {
					errorMsg = e.getClass().getName() + ": " + e.getMessage();			
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		
		job.schedule();
		job.join();
		
		return DatabaseStatus.OK;
	}
	
	/***
	 * Retrieve the last error message.
	 * If the last status is {@code DatabaseStatus.OK}, it returns {@code null}.
	 * 
	 * @return {@code String} 
	 * 			the last error message if the last status is not {@code DatabaseStatus.OK}
	 */
	public String getErrorMessage() {
		if (status == DatabaseStatus.OK)
			return null;
		
		if (errorMsg != null)
			return errorMsg;
		
		return "Unknown error";
	}

	@Override
	public DatabaseStatus getStatus() {
		return status;
	}

	@Override
	public boolean hasTraceData() {
		return experiment.getTraceDataVersion() > 0;
	}

	@Override
	public ITraceManager getORCreateTraceManager() throws InvalExperimentException, IOException {
		if (traceManager == null) {
			var opener = new LocalDBOpener(experiment);
			var progressGroup = Job.getJobManager().createProgressGroup();
			traceManager = opener.openDBAndCreateSTDC(progressGroup);
		}
		return traceManager;
	}

	@Override
	public String getSourceFileContent(SourceFile fileId) throws IOException {
		if (fileId instanceof EmptySourceFile || !fileId.isAvailable())
			return null;
		
		FileSystemSourceFile source = (FileSystemSourceFile) fileId;
		var filename = source.getCompleteFilename();
		Path path = Path.of(filename);

		return Files.readString(path, StandardCharsets.ISO_8859_1);
	}

	@Override
	public boolean isSourceFileAvailable(Scope scope) {		
		return Util.isFileReadable(scope);
	}


	@Override
	public RootScope createFlatTree(Scope rootCCT, RootScope rootFlat, IProgressReport progressMonitor) {
		return experiment.createFlatView(rootCCT, rootFlat, progressMonitor);
	}
}
