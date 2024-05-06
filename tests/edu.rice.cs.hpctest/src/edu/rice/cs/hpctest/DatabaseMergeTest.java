package edu.rice.cs.hpctest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import edu.rice.cs.hpcbase.IDatabase;
import edu.rice.cs.hpcbase.IDatabaseIdentification;
import edu.rice.cs.hpcbase.ITraceManager;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.InvalExperimentException;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.source.SourceFile;
import edu.rice.cs.hpcdata.merge.DatabasesToMerge;
import edu.rice.cs.hpcdata.util.IProgressReport;
import edu.rice.cs.hpcmerge.DatabaseMergeWizard;
import edu.rice.cs.hpctest.util.TestDatabase;

public class DatabaseMergeTest {

	public static void main(String[] args) throws Exception {
		System.out.println("Test begin");
		final Display display = new Display();
		final Shell   shell   = new Shell(display);
		shell.setLayout(new FillLayout());
		
		shell.setText("Test wizard");
		

		var dirs = TestDatabase.getDatabases();
		List<IDatabase> list = new ArrayList<>(dirs.length);
		for(var d: dirs) {
			var exp1 = new Experiment();
			
			var localDb = new LocalDatabaseRepresentation(d, null, IProgressReport.dummy());
			exp1.open(localDb);
			list.add(new DatabaseWrapper(exp1));
		}

		DatabaseMergeWizard wz = new DatabaseMergeWizard(list);
		WizardDialog wd = new WizardDialog(shell, wz);
		int result = wd.open();
		if (result == Dialog.OK) {
			DatabasesToMerge dm = wz.getDatabaseToMerge();
			assert(dm != null);
			
			System.out.println("To merge: ");
			System.out.println("\t" + dm.experiment[0].getExperimentFile().getAbsolutePath() + " metric: " + dm.metric[0].getDisplayName());
			System.out.println("\t" + dm.experiment[1].getExperimentFile().getAbsolutePath() + " metric: " + dm.metric[1].getDisplayName());
		}
		display.dispose();

		System.out.println("Test end");
	}

	
	static class DatabaseWrapper implements IDatabase
	{
		Experiment experiment;
		
		DatabaseWrapper(Experiment experiment) {
			this.experiment = experiment;
		}
		@Override
		public IDatabaseIdentification getId() {
			return () -> experiment.getID();
		}

		@Override
		public DatabaseStatus open(Shell shell) {
			return DatabaseStatus.OK;
		}

		@Override
		public DatabaseStatus reset(Shell shell, IDatabaseIdentification id) {
			return DatabaseStatus.OK;
		}

		@Override
		public DatabaseStatus getStatus() {
			return DatabaseStatus.OK;
		}

		@Override
		public void close() {
			
		}

		@Override
		public IMetricManager getExperimentObject() {
			return experiment;
		}

		@Override
		public boolean hasTraceData() {
			return false;
		}

		@Override
		public ITraceManager getORCreateTraceManager() throws IOException, InvalExperimentException {
			return null;
		}

		@Override
		public String getSourceFileContent(SourceFile fileId) throws IOException {
			return null;
		}

		@Override
		public boolean isSourceFileAvailable(Scope scope) {
			return false;
		}

		@Override
		public RootScope createFlatTree(Scope rootCCT, RootScope rootFlat, IProgressReport progressMonitor) {
			return null;
		}

		@Override
		public String getErrorMessage() {
			return null;
		}
		
	}
}
