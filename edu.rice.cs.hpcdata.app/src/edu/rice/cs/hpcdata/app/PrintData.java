package edu.rice.cs.hpcdata.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.TreeNode;
import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpcdata.util.ScopeComparator;
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
	
	/***
	 * The main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		PrintData objApp = new PrintData();
		PrintStream objPrint = System.out;
		String sFilename;
		boolean std_output = true;
		
		//------------------------------------------------------------------------------------
		// processing the command line argument
		//------------------------------------------------------------------------------------
		if ( (args == null) || (args.length==0)) {
			System.out.println("Usage: hpcdata.sh [-o output_file] experiment_database");
			return;
		} else  {
			sFilename = args[0];
			
			for (int i=0; i<args.length; i++) {
				if (args[i].equals("-o") && (i<args.length-1)) {
					String sOutput = args[i+1];
					File f = new File(sOutput);
					if (!f.exists())
						try {
							f.createNewFile();
						} catch (IOException e1) {
							e1.printStackTrace();
							return;
						}
					try {
						FileOutputStream file = new FileOutputStream(sOutput);
						try {
							objPrint = new PrintStream( file );
							std_output = false;
							i++;
						} catch (Exception e) {
							System.err.println("Error: cannot create the file " + sOutput + ": " +e.getMessage());
							return;
						}

					
					} catch (FileNotFoundException e2) {
						System.err.println("Error: cannot open the file " + sOutput + ": " +e2.getMessage());
						e2.printStackTrace();
					}
				} else {
					sFilename = args[i];
				}
			}
		}
		
		//------------------------------------------------------------------------------------
		// open the experiment if possible
		//------------------------------------------------------------------------------------
		PrintStream print_msg;
		if (std_output)
			print_msg = System.err;
		else
			print_msg = System.out;
		
		File objFile = new File(sFilename);

		print_msg.println("Opening database " + sFilename);
		
		Experiment experiment = null;
		
		if (objFile.isDirectory()) {
			File files[] = Util.getListOfXMLFiles(sFilename);
			for (File file: files) 
			{
				// only experiment*.xml will be considered as database file
				if (file.getName().startsWith(Constants.DATABASE_FILENAME)) {
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
		
		//------------------------------------------------------------------------------------
		// print information
		//------------------------------------------------------------------------------------
		
		final int MegaBytes = 1024*1024;
	    long maxMemory = Runtime.getRuntime().maxMemory()/MegaBytes;

		objPrint.println("Max memory: " + maxMemory + " MB");
		objPrint.println();
		
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
		
		List<TreeNode> roots = experiment.getRootScopeChildren();
		
		for(Object root: roots) {

			RootScope aRoot = (RootScope) root;
			objPrint.println("Summary of " + aRoot.getType() + "\n");
			
			// print root CCT metrics
			printScopeAndChildren(objPrint, aRoot, experiment);
		}
	}

	
	static private void printScopeAndChildren(PrintStream objPrint, Scope scope, Experiment experiment) {
		
		List<BaseMetric> metrics = experiment.getVisibleMetrics();
		
		// print root CCT metrics
		printRootMetrics(objPrint, scope, metrics);
		if (!scope.hasChildren())
			return;
		
		// sort the children from the highest value to the lowest based on the first metric
		
		List<? extends TreeNode> children = scope.getChildren();
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
		
		@SuppressWarnings("unchecked")
		List<Scope> childrenScope = (List<Scope>) children;
		childrenScope.sort(comparator);
		
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
			String metricName = getTrimmedName(metric.getDisplayName(), 12);
			System.out.print(String.format(" [%3d] %s", index, metricName));
		}
		
		// print the first 5 children
		for(int i=0; i<Math.min(5, childrenScope.size()); i++) {
			objPrint.println();
			
			Scope child = (Scope) childrenScope.get(i);
			printMetrics(objPrint, child, metrics, nonEmptyIndex, "   ");
		}
	}
	
	
	static private String getTrimmedName(String name, int maxChars) {
		if (name.length()>maxChars) {
			name = name.substring(0, maxChars-3) + " ..";
		} else {
			name = String.format("%" + (-maxChars) + "s", name);
		}
		return name;
	}
	
	
	static private String getScopeName(Scope scope) {
		String name = getTrimmedName(scope.getName(), MAX_NAME_CHARS);
		return name;
	}
	
	
	static private void printRootMetrics(PrintStream objPrint, Scope scope, List<BaseMetric> metrics) {
		String name = getScopeName(scope);
		objPrint.println("- " + name);
		
		for(int i=0; i<metrics.size(); i++) {
			BaseMetric metric = metrics.get(i);
			String metricName = getTrimmedName(metric.getDisplayName(), MAX_METRIC_NAME);
			objPrint.print(String.format("\t [%3d] %s", i, metricName));
			if (scope.getMetricValue(metric) == MetricValue.NONE) {
				objPrint.println("            0.0");
			} else  {
				String out = metric.getMetricTextValue(scope).substring(0, 15);
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
	static private void printMetrics(PrintStream objPrint, 
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
