package edu.rice.cs.hpcviewer.ui.experiment;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import edu.rice.cs.hpcbase.map.ProcedureAliasMap;
import edu.rice.cs.hpcdata.experiment.Experiment;

public class ExperimentJob extends Job 
{
	final private String filename;
	private Exception e;

	public ExperimentJob(String name, String filename) {
		super(name);

		this.filename = filename;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		Experiment experiment = new Experiment();
		
		try {
			experiment.open( new File(filename), new ProcedureAliasMap(), true );
		} catch (Exception e) {

			this.e = e;
		}
		return Status.OK_STATUS;
	}
	
	public Exception getException() {
		return e;
	}

}
