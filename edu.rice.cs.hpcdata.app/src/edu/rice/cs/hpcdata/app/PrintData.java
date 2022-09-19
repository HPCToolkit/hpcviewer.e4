package edu.rice.cs.hpcdata.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import edu.rice.cs.hpcdata.db.DatabaseManager;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.util.Util;


/****************************************************************
 * 
 * Class to print the summary of a database
 * 
 * <p>
 * Usage: hpcdata.sh [-o output_file] experiment_database
 * </p>
 *
 ****************************************************************/
public class PrintData 
{
	private static final int MAX_METRIC_NAME = 32;
	private static final int MAX_NAME_CHARS = 44;
	
	private final static int DISPLAY_TOPDOWN  = 1;
	private final static int DISPLAY_BOTTOMUP = 2;
	private final static int DISPLAY_FLAT     = 4;
	private final static int DISPLAY_EXCLUSIVE = 8;
	private final static int DISPLAY_INCLUSIVE = 16;
	
	private final static int DISPLAY_ALLVIEWS = DISPLAY_TOPDOWN   | DISPLAY_BOTTOMUP | DISPLAY_FLAT |
												DISPLAY_EXCLUSIVE | DISPLAY_INCLUSIVE;
	
	private final static int NODES_SUMMARY = 0;
	private final static int NODES_ALL     = 16;
	
	private PrintData() {
		// hide the constructor
		// use the static method openExperiment instead
	}
	
	/***
	 * The main method
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		PrintStream objPrint = System.out;
		String sFilename = null;
		boolean std_output = true;

		int display_mode = 0;

		//------------------------------------------------------------------------------------
		// processing the command line argument
		//------------------------------------------------------------------------------------
		if ( (args == null) || (args.length==0) || args[0].equals("-h") || args[0].equals("--help")) {
			PrintData.showHelp();
			return;
		}
		for (int i=0; i<args.length; i++) {
			final String option = args[i];
			switch(option) {
			case "-a":
				display_mode |=  NODES_ALL;
				break;
			case "-b":
				display_mode |=  DISPLAY_BOTTOMUP;
				break;
			case "-e":
				display_mode |=  DISPLAY_EXCLUSIVE;
				break;
			case "-f":
				display_mode |=  DISPLAY_FLAT;
				break;
			case "-i":
				display_mode |=  DISPLAY_INCLUSIVE;
				break;
			case "-s":
				display_mode |=  NODES_SUMMARY;
				break;
			case "-t":
				display_mode |=  DISPLAY_TOPDOWN;
				break;
			case "-o":
				if (i<args.length-1) {
					String sOutput = args[i+1];
					File f = new File(sOutput);
					if (!f.exists())
						f.createNewFile();
					
					FileOutputStream file = new FileOutputStream(sOutput);
					objPrint = new PrintStream( file );
					std_output = false;
					i++;
					file.close();
				}
				break;
				
			default:
				if (!option.startsWith("-"))
					sFilename = option;
			}				
		}
		if (sFilename == null) {
			PrintData.showHelp();
			return;
		}
		
		if (display_mode == 0)
			display_mode = DISPLAY_ALLVIEWS;
		
		//------------------------------------------------------------------------------------
		// open the experiment if possible
		//------------------------------------------------------------------------------------
		openExperiment(sFilename, objPrint, display_mode, std_output);
	}
	
	private static void openExperiment(String sFilename, PrintStream objPrint, int display_mode, boolean std_output) {
		PrintStream print_msg;
		if (std_output)
			print_msg = System.err;
		else
			print_msg = System.out;
		
		File objFile = new File(sFilename);

		print_msg.println("Opening database " + sFilename);
		
		Experiment experiment = null;
		PrintData objApp = new PrintData();

		if (objFile.isDirectory()) {
			File files[] = Util.getListOfXMLFiles(sFilename);
			for (File file: files) 
			{
				// only experiment*.xml will be considered as database file
				if (DatabaseManager.isDatabaseFile(file.getName())) {
					experiment = objApp.openExperiment(print_msg, file);
					if (experiment != null)
						break;
					
				}
			}
		} else {
			experiment = objApp.openExperiment(objPrint, objFile);
		}
		if (experiment == null) {
			print_msg.println("Incorrect database: " + objFile.getAbsolutePath());
			return;
		}
		print(experiment, objPrint, display_mode);
	}
	
	
	public static void showHelp() {
		System.out.println("Usage: hpcdata.sh [Options] experiment_database");
		System.out.println("Options: ");
		System.out.println("     -o output_file");
		System.out.println("     -a display all the tree nodes");
		System.out.println("     -b display the bottom-up view only ");
		System.out.println("     -f display the flat view only ");
		System.out.println("     -t display the top-down view only");
		System.out.println("     -s display only the first 5 nodes (default)");
		
		System.exit(0);
	}
	
	
	public static void print(Experiment experiment, PrintStream objPrint, int display_mode) {
		
		//------------------------------------------------------------------------------------
		// create derivative roots: bottom-up and flat trees
		// these trees are not created automatically for the sake of memory usage
		//------------------------------------------------------------------------------------

		RootScope rootCCT = experiment.getRootScope(RootScopeType.CallingContextTree);
		
		RootScope rootFlat = experiment.getRootScope(RootScopeType.Flat);
		experiment.createFlatView(rootCCT, rootFlat);
		
		RootScope rootBottomUp = experiment.getRootScope(RootScopeType.CallerTree);
		experiment.createCallersView(rootCCT, rootBottomUp);
		
		//------------------------------------------------------------------------------------
		// print the summary
		//------------------------------------------------------------------------------------
		
		var roots = experiment.getRootScopeChildren();
		
		for(Object root: roots) {
			RootScope aRoot = (RootScope) root;
			boolean displayRoot = (aRoot.getType() == RootScopeType.CallingContextTree && 
				    				(display_mode & DISPLAY_TOPDOWN) != 0)  ||
								  (aRoot.getType() == RootScopeType.CallerTree && 
								    (display_mode & DISPLAY_BOTTOMUP) != 0)  ||
								  (aRoot.getType() == RootScopeType.Flat && 
								    (display_mode & DISPLAY_FLAT) != 0) 
								  ;
			if (displayRoot) {
				objPrint.println("\nSummary of " + aRoot.getType());
				
				// print root CCT metrics
				printScopeAndChildren(objPrint, aRoot, experiment, display_mode);
			}
		}
	}

	
	private static void printScopeAndChildren(PrintStream objPrint, 
											  Scope scope, 
											  Experiment experiment,
											  int display_mode) {
		
		List<BaseMetric> metrics = experiment.getVisibleMetrics();
		
		// print root CCT metrics
		printRootMetrics(objPrint, scope, metrics);
		if (!scope.hasChildren())
			return;
		
		// sort the children from the highest value to the lowest based on the first metric
		
		var children = scope.getChildren();
		List<Integer> nonEmptyIds = experiment.getNonEmptyMetricIDs(scope);		
		BaseMetric sortMetric = metrics.get(0);
		
		for(int i=0; i<nonEmptyIds.size(); i++) {
			int id = nonEmptyIds.get(i);
			BaseMetric m = experiment.getMetric(id);
			if (m.getMetricType() == MetricType.INCLUSIVE) {
				sortMetric = m;
				break;
			}
		}
		
		ScopeComparator comparator = new ScopeComparator();
		comparator.setMetric(sortMetric);
		comparator.setDirection(ScopeComparator.SORT_DESCENDING);

		children.sort(comparator);
		
		List<Integer> nonEmptyIndex = new ArrayList<Integer>(nonEmptyIds.size());
		for(int i=0; i<metrics.size(); i++) {
			BaseMetric metric = metrics.get(i);
			if (metric.getValue(scope) != MetricValue.NONE) {
				nonEmptyIndex.add(i);
			}
		}
		
		// print the metric header
		System.out.print(String.format("\n%" + (4+MAX_NAME_CHARS) + "s", " "));
		for(Integer index: nonEmptyIndex) {
			BaseMetric metric = metrics.get(index);
			String metricName = getTrimmedName(metric.getDisplayName(), 11);
			System.out.print(String.format(" [%3d] %s ", index, metricName));
		}
		
		// print the children
		boolean displayAll = (display_mode & NODES_ALL) != 0;
		int numChildren = displayAll ? children.size() : Math.min(5, children.size());
		for(int i=0; i<numChildren; i++) {
			objPrint.println();
			
			Scope child = (Scope) children.get(i);
			printMetrics(objPrint, child, metrics, nonEmptyIndex, "   ");
		}
	}
	
	
	private static String getTrimmedName(String name, int maxChars) {
		if (name.length()>maxChars) {
			name = name.substring(0, maxChars-3) + " ..";
		} else {
			name = String.format("%" + (-maxChars) + "s", name);
		}
		return name;
	}
	
	
	private static String getScopeName(Scope scope) {
		String name = getTrimmedName(scope.getName(), MAX_NAME_CHARS);
		return name;
	}
	
	
	private static void printRootMetrics(PrintStream objPrint, Scope scope, List<BaseMetric> metrics) {
		String name = getScopeName(scope);
		objPrint.println("- " + name);
		
		for(int i=0; i<metrics.size(); i++) {
			BaseMetric metric = metrics.get(i);
			String metricName = getTrimmedName(metric.getDisplayName(), MAX_METRIC_NAME);
			objPrint.print(String.format("\t [%3d] %s", i, metricName));
			if (scope.getMetricValue(metric) == MetricValue.NONE) {
				objPrint.println("            0.0");
			} else  {
				var value  = metric.getMetricTextValue(scope);
				var lenVal = Math.min(15, value.length());
				String out = value.substring(0, lenVal);
				objPrint.println(out);
			}
		}
	}
	
	
	/****
	 * Print metric values of a given scope
	 * 
	 * @param objPrint print stream (either file or standard output)
	 * @param scope the node scope
	 * @param metrics list of metrics
	 * @param indent output indentation
	 */
	private static void printMetrics(PrintStream objPrint, 
									 Scope scope, 
									 List<BaseMetric> metrics,
									 List<Integer> nonEmptyIndex,
									 String indent) {
		String name = getScopeName(scope);
		objPrint.print(indent + "- " + name);
		
		for(Integer index: nonEmptyIndex) {
			BaseMetric metric = metrics.get(index);
			objPrint.print(indent + " ");
			if (scope.getMetricValue(metric) == MetricValue.NONE) {
				objPrint.print("            0.0");
			} else  {
				String out = metric.getMetricTextValue(scope).substring(0, 15);
				objPrint.print(out);
			}
		}
	}
	
	/***
	 * Open a database if exist and valid
	 * @param objPrint  stream to print (either standard out or err)
	 * @param objFile   The XML file of the database
	 * @return experiment file if exists, null if fails.
	 */
	private Experiment openExperiment(PrintStream objPrint, File objFile) {

		try {
			Experiment experiment = new Experiment();	// prepare the experiment
			experiment.open(objFile, null, Experiment.ExperimentOpenFlag.TREE_ALL);	// parse the database

			return experiment;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}	
}
