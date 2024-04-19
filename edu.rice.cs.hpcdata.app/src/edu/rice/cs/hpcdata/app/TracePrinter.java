package edu.rice.cs.hpcdata.app;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.db.IFileDB;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.version2.TraceDB2;
import edu.rice.cs.hpcdata.db.version4.FileDB4;
import edu.rice.cs.hpcdata.db.version4.MetricValueCollection4;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.LocalDatabaseRepresentation;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.trace.TraceRecord;
import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpcdata.util.IProgressReport;

/*****
 * 
 * Class to print the content of hpctrace file.
 * If the trace file is not merged, it will be merged automatically
 * 
 * To run the program:
 * 
 *   java TracePointer database_directory [thread_to_print]
 *   
 ****/
public class TracePrinter 
{
	public static void main(String[] args) throws IOException {
		
		if (args == null || args.length < 1) {
			System.out.println("Syntax: java TracePrinter <database_directory>  [thread_to_print]");
			return;
		}
		
		// ------------------------------------------------------------------------
		// create experiment object by reading the experiment.xml and extract 
		// some information concerning hpctraces
		// we don't need this step, but it's best practice
		// ------------------------------------------------------------------------
		
		final Experiment experiment = new Experiment();
		LocalDatabaseRepresentation localDb = new LocalDatabaseRepresentation(new File(args[0]), null, IProgressReport.dummy());
		try {
			experiment.open(localDb);
		} catch (Exception e) {
			return;
		}

		// ------------------------------------------------------------------------
		// open, read and merge (if necessary) hpctrace files
		// ------------------------------------------------------------------------

		IFileDB fileDB;
		if (experiment.getMajorVersion() == Constants.EXPERIMENT_DENSED_VERSION) {
			fileDB = new TraceDB2(experiment);
		} else if (experiment.getMajorVersion() == Constants.EXPERIMENT_SPARSE_VERSION) {
			var root = experiment.getRootScope(RootScopeType.CallingContextTree);
			MetricValueCollection4 mvc = (MetricValueCollection4) root.getMetricValueCollection();
			fileDB = new FileDB4(experiment, mvc.getDataSummary());
		} else {
			System.err.println("Unknown database version: " + localDb.getId());
			return;
		}
		
		 
		try {
			fileDB.open(args[0]);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		if (args.length == 1) {
			System.out.println("File: " + args[0]);
			printSummary(fileDB);
			return;
		}
		for(int i=1; i<args.length; i++) {
			final var ranks = fileDB.getIdTuple(IdTupleOption.BRIEF);
			int j=0;
			for(var profile: ranks) {
				if (Integer.valueOf(args[i]) == j) {
					System.out.println("File: " + args[i]);
					try {
						printTrace(experiment, profile, fileDB);
						System.out.println("------------------------");
					} catch (IOException e) {
						System.err.println(args[i] + "Unknown rank");
						return;
					}
				}
			}
		}
	}

	
	private static void printTrace(Experiment experiment, IdTuple rank, IFileDB fileDB) throws IOException {
		
		final int MAX_NAME = 16;
		
		var map = experiment.getScopeMap(); 
		
		TraceReader reader = new TraceReader(fileDB);
		long numRecords = reader.getNumberOfRecords(rank);
		
		TraceRecord prevRecord = reader.getData(rank, 0);
		
		for(long i=1; i<numRecords; i++) {
			TraceRecord newRecord = reader.getData(rank, i);
			long delta  = newRecord.timestamp - prevRecord.timestamp;
			String name = String.valueOf(prevRecord.cpId);
			
			if (map != null) {
				var scope = map.getCallPathScope(prevRecord.cpId);
				if (scope != null) {
					name = scope.getName();
					if (name.length() > MAX_NAME)
						name = name.substring(0, MAX_NAME) + "...";
				}
			}

			System.out.printf( "%8d. %-20s : %,d ns%n", prevRecord.cpId, name, delta);

			prevRecord = newRecord;
		}
	}
	
	private static void printSummary(IFileDB fileDB) {
		final String []ranks = fileDB.getRankLabels();
		if (ranks == null) return;
		
		System.out.println("\nParallelism level: " + fileDB.getParallelismLevel());
		System.out.println("Rank: ");

		var list = fileDB.getIdTuple(IdTupleOption.BRIEF);
		
		for(var profile: list) {
			long minLoc = fileDB.getMinLoc(profile);
			long maxLoc = fileDB.getMaxLoc(profile);
			long numBytes = maxLoc - minLoc;
			System.out.printf(Locale.US, "  %8s (%,d - %,d) : %,d bytes\n", profile.toString(), minLoc, maxLoc, numBytes);
		}
	}
}
