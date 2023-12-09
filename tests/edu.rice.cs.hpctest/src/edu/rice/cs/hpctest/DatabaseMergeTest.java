package edu.rice.cs.hpctest;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
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
		List<Experiment> list = new ArrayList<>(2);
		var exp1 = new Experiment();
		
		var localDb = new LocalDatabaseRepresentation(dirs[0], null, IProgressReport.dummy());
		exp1.open(localDb);
		list.add(exp1);
		
		var exp2 = new Experiment();
		exp2.open(localDb);		
		list.add(exp2);

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
		
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		 
		display.dispose();

		System.out.println("Test end");
	}

}
