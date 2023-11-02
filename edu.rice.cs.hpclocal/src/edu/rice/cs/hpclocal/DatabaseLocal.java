package edu.rice.cs.hpclocal;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcbase.ITraceManager;
import edu.rice.cs.hpcbase.map.ProcedureAliasMap;
import edu.rice.cs.hpcdata.db.DatabaseManager;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.InvalExperimentException;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.source.EmptySourceFile;
import edu.rice.cs.hpcdata.experiment.source.FileSystemSourceFile;
import edu.rice.cs.hpcdata.experiment.source.SourceFile;
import edu.rice.cs.hpcdata.util.Util;

public class DatabaseLocal implements IDatabaseLocal 
{
	private Experiment experiment;
	private String errorMsg;
	private DatabaseStatus status = DatabaseStatus.NOT_INITIALIZED;
	private ITraceManager traceManager;
	
	@Override
	public String getId() {
		if (experiment != null)
			return experiment.getID();
		
		return getClass().getName();
	}

	@Override
	public DatabaseStatus open(Shell shell) {
		var experimentManager = new ExperimentManager();
		try {
			var filename = experimentManager.openFileExperiment(shell);
			if (filename != null && !filename.isEmpty()) {
				status = setDirectory(filename);
			} else {
				errorMsg = filename + ": invalid directory.";
				status = DatabaseStatus.INVALID;
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
	public IExperiment getExperimentObject() {
		return experiment;
	}

	@Override
	public String getDirectory() {
		return experiment.getID();
	}

	@Override
	public DatabaseStatus setDirectory(String fileOrDirectory) {
		status = DatabaseStatus.INVALID;
		if (fileOrDirectory == null) {
			return status;
		}
		
		String filename = fileOrDirectory;
		
		if (!Files.isRegularFile(Paths.get(fileOrDirectory)) && ExperimentManager.checkDirectory(fileOrDirectory)) {
			var filepath = DatabaseManager.getDatabaseFilePath(fileOrDirectory);
			if (filepath.isEmpty()) {
				errorMsg = fileOrDirectory + ": is not a HPCToolkit database";
				
				status = DatabaseStatus.INEXISTENCE;
				
				return status;				
			}
			filename = filepath.get();
		}
		var file = new File(filename);
		
		if (!file.canRead()) {
			errorMsg = fileOrDirectory + ": is not accessible";
			return DatabaseStatus.INVALID;
		}
		experiment = new Experiment();
		
		ProcedureAliasMap map = new ProcedureAliasMap();		
		var localDb = new LocalDatabaseRepresentation(file, map, true);
		
		try {
			experiment.open(localDb);
		} catch (Exception e) {
			errorMsg = e.getClass().getName() + ": " + e.getMessage();			
			return status;
		}
		status = DatabaseStatus.OK;
		return status;
	}
	
	
	public String getErrorMessage() {
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
}
