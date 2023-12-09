package edu.rice.cs.hpcdata.app;

import java.io.File;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.merge.DatabasesToMerge;
import edu.rice.cs.hpcdata.merge.ExperimentMerger;
import edu.rice.cs.hpcdata.merge.TreeSimilarity;
import edu.rice.cs.hpcdata.util.IProgressReport;

public class ApplicationMerge {

	/***---------------------------------------------------------------------**
	 * Open a XML database file, and return true if everything is OK.
	 * @param objFile: the XML experiment file
	 * @return
	 ***---------------------------------------------------------------------**/
	private static Experiment openExperiment(File objFile) {
		Experiment experiment = new Experiment();	// prepare the experiment;
		var localDb = new LocalDatabaseRepresentation(objFile, null, IProgressReport.dummy());
		try {
			experiment.open(localDb);	// parse the database

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
		DatabasesToMerge db = new DatabasesToMerge();
		db.experiment[0] = exp1;
		db.experiment[1] = exp2;
		db.type = RootScopeType.CallingContextTree;
		
		try {
			ExperimentMerger.merge(db);
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
