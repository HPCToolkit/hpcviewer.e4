package edu.rice.cs.hpcdata.framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.xml.PrintFileXML;
import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpcdata.util.Util;

/******************************************************************************************
 * Class to manage the execution of the light version of hpcviewer
 * This class will require an argument for a database or XML file, then
 * 	output the result into XML file
 * Otherwise, output error message.
 * No user interaction is needed in this light version
 * @author laksonoadhianto
 *
 ******************************************************************************************/
public class Application {

	
	/***---------------------------------------------------------------------**
	 * Open a XML database file, and return true if everything is OK.
	 * @param objFile: the XML experiment file
	 * @return
	 ***---------------------------------------------------------------------**/
	private boolean openExperiment(PrintStream objPrint, File objFile) {
		Experiment experiment;

		try {
			experiment = new Experiment();	// prepare the experiment
			experiment.open(objFile, null, Experiment.ExperimentOpenFlag.TREE_ALL);	// parse the database
			this.printFlatView(objPrint, experiment);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	/***---------------------------------------------------------------------**
	 * 
	 * @param experiment
	 ***---------------------------------------------------------------------**/
	private void printFlatView(PrintStream objPrint, Experiment experiment) {
		PrintFileXML objPrintXML = new PrintFileXML();
		objPrintXML.print(objPrint, experiment);
	}
	
	
	/**---------------------------------------------------------------------**
	 * Main application
	 * @param args
	 **---------------------------------------------------------------------**/
	public static void main(String[] args) {
		Application objApp = new Application();
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
							System.err.println("Error: cannot create file " + sOutput + ": " +e.getMessage());
							return;
						}

					
					} catch (FileNotFoundException e2) {
						// TODO Auto-generated catch block
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
		boolean done = false;

		print_msg.println("Opening database " + sFilename);
		
		if (objFile.isDirectory()) {
			File files[] = Util.getListOfXMLFiles(sFilename);
			for (File file: files) 
			{
				// only experiment*.xml will be considered as database file
				if (file.getName().startsWith(Constants.DATABASE_FILENAME)) {
					done = objApp.openExperiment(objPrint, file);
					if (done)
						break;
				}
			}
		} else {
			done = objApp.openExperiment(objPrint, objFile);
			
		}

		if (done)
			print_msg.println("Flat view has been generated successfully");
	}

}
