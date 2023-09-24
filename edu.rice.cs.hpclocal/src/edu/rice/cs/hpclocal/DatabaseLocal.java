package edu.rice.cs.hpclocal;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcbase.ITraceManager;
import edu.rice.cs.hpcbase.map.ProcedureAliasMap;
import edu.rice.cs.hpcdata.db.DatabaseManager;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;

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
			}
		} catch (Exception e) {
			MessageDialog.openError(shell, "File to open the database", e.getMessage());
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
		if (errorMsg == null)
			return errorMsg;
		
		errorMsg = null;
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
	public ITraceManager getORCreateTraceManager() {
		return traceManager;
	}
	
	
	@Override
	public void setTraceManager(ITraceManager traceManager) {
		this.traceManager = traceManager;
	}
}
