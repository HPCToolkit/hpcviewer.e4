package edu.rice.cs.hpc.data.framework;

import java.io.File;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.merge.ExperimentMerger;
import edu.rice.cs.hpc.data.experiment.merge.TreeSimilarity;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;

public class ApplicationMerge {

	/***---------------------------------------------------------------------**
	 * Open a XML database file, and return true if everything is OK.
	 * @param objFile: the XML experiment file
	 * @return
	 ***---------------------------------------------------------------------**/
	private static Experiment openExperiment(File objFile) {
		Experiment experiment;

		try {
			experiment = new Experiment();	// prepare the experiment
			experiment.open(objFile, null, false);						// parse the database

			return experiment;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length != 2) {
			showHelp();
			return;
		}
		File f1 = new File(args[0]);
		Experiment exp1 = openExperiment(f1);
		if (exp1 == null) {
			System.err.println("Cannot open database: " + args[0]);
			return;
		}
		
		File f2 = new File(args[1]);
		Experiment exp2 = openExperiment(f2);
		if (exp2 == null) {
			System.err.println("Cannot open database: " + args[1]);
		}
		System.setProperty(TreeSimilarity.PROP_DEBUG, "true");
		
		try {
			ExperimentMerger.merge(exp1, exp2, RootScopeType.CallingContextTree);
		} catch (Exception e) {
			System.err.println("Fail to merge");
			e.printStackTrace();
		}
	}

	private static void showHelp() 
	{
		System.out.println("Syntax: ApplicationMerge file_db1 file_db2");
	}
}
