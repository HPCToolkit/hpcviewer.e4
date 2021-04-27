package edu.rice.cs.hpcdata.experiment.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.visitors.PrintFlatViewScopeVisitor;


/***************************************************************************************
 * Class to print the content of an experiment database into an output stream
 * @author laksonoadhianto
 *
 ***************************************************************************************/
public class PrintFileXML {
	//-----------------------------------------------------------------------
	// Constants
	//-----------------------------------------------------------------------
	final private String DTD_FILE_NAME = "experiment.dtd";
	final private int MAX_BUFFER = 1024;

	
	/**--------------------------------------------------------------------------------**
	 * print an experiment into a given output stream
	 * @param objPrint
	 * @param experiment
	 **--------------------------------------------------------------------------------**/
	public void print(PrintStream objStream, Experiment experiment) {
		RootScope cctRoot  = experiment.getRootScope(RootScopeType.CallingContextTree);
		RootScope flatRoot = experiment.getRootScope(RootScopeType.Flat);
		experiment.createFlatView(cctRoot, flatRoot);
		if (flatRoot != null) {
			
			//---------------------------------------------------------------------------------
			// print the DTD
			//---------------------------------------------------------------------------------
			this.printDTD(objStream);
			
			//---------------------------------------------------------------------------------
			// print the header
			//---------------------------------------------------------------------------------
			this.printHeader(objStream, experiment);
			
			//---------------------------------------------------------------------------------
			// print the content
			//---------------------------------------------------------------------------------
			objStream.println("<SecFlatProfileData>");
			PrintFlatViewScopeVisitor objPrintFlat = new PrintFlatViewScopeVisitor(experiment, objStream);
			flatRoot.dfsVisitScopeTree(objPrintFlat);
			
			//---------------------------------------------------------------------------------
			// print the footer
			//---------------------------------------------------------------------------------
			objStream.println("</SecFlatProfileData>\n</SecFlatProfile>\n</HPCToolkitExperiment>");
		} else {
			System.err.println("The database contains no information");
		}

	}
	
	
	/**--------------------------------------------------------------------------------**
	 * Static method to print an attribute and its value to a specific format
	 * @param objPrint
	 * @param attribute
	 * @param value
	 **--------------------------------------------------------------------------------**/
	static public void printAttribute(PrintStream objPrint, String attribute, Object value) {
		objPrint.print(" "+ attribute + "=\"" + value + "\"");
	}

	
	/**--------------------------------------------------------------------------------**
	 * 
	 * @param objPrint
	 * @param experiment
	 **--------------------------------------------------------------------------------**/
	private void printMetricTable(PrintStream objPrint, Experiment experiment) {
		objPrint.println(" <MetricTable>");
		List<BaseMetric> metrics = experiment.getMetricList();
		for(BaseMetric m: metrics) {
			objPrint.print("    <Metric"); 
			{
				printAttribute(objPrint, "i", m.getIndex());				
				printAttribute(objPrint, "n", m.getDisplayName().trim());				
				printAttribute(objPrint, "v", "final");				
				printAttribute(objPrint, "t", getMetricType(m) );				
				printAttribute(objPrint, "show", booleanToInt(m.getDisplayed()));
				printAnnotationType(objPrint, m);
			}
			objPrint.print(">");

			objPrint.println(" </Metric>");
		}
		objPrint.println(" </MetricTable>");
	}
	
	
	/**--------------------------------------------------------------------------------**
	 * 
	 * @param m
	 * @return
	 **--------------------------------------------------------------------------------**/
	private String getMetricType (BaseMetric m) {
		if (m.getMetricType() == MetricType.EXCLUSIVE )
			return "exclusive";
		else if (m.getMetricType() == MetricType.INCLUSIVE )
			return "inclusive";
		return "nil";
	}
	
	
	/**--------------------------------------------------------------------------------**
	 * print the type of annotation
	 * @param objPrint
	 * @param m
	 **--------------------------------------------------------------------------------**/
	private void printAnnotationType (PrintStream objPrint, BaseMetric m) {
		
		switch (m.getAnnotationType()) {
		case PERCENT:
			printAttribute(objPrint, "show-percent", "1" );
			break;
		
		case PROCESS:
			printAttribute(objPrint, "show-process", "1" );
			break;
			
		case NONE:
			break;
		}
	}
	
	
	/**--------------------------------------------------------------------------------**
	 * 
	 * @param b
	 * @return
	 **--------------------------------------------------------------------------------**/
	private int booleanToInt(boolean b) {
		if (b)
			return 1;
		else
			return 0;
	}
	
	
	/**--------------------------------------------------------------------------------**
	 * 
	 * @param objPrint
	 * @param experiment
	 **--------------------------------------------------------------------------------**/
	private void printHeader(PrintStream objPrint, Experiment experiment) {
		objPrint.println("<HPCToolkitExperiment version=\"" + experiment.getMajorVersion() + "\">");

		objPrint.print("<Header");
		PrintFileXML.printAttribute( objPrint, "n", experiment.getName() );
		objPrint.println(">\n  <Info/>\n</Header>");
		
		objPrint.print("<SecFlatProfile ");
		PrintFileXML.printAttribute( objPrint, "i", "0");
		PrintFileXML.printAttribute( objPrint, "n", experiment.getName() );
		objPrint.println(">\n<SecHeader>");
		
		this.printMetricTable(objPrint, experiment);
		
		objPrint.println("</SecHeader>");
	}

	
	/**---------------------------------------------------------------------**
	 * Printing DTD of an experiment. The sample of DTD is located in edu.rice.cs.hpc.data.experiment.xml package
	 * This method will first load the file, then print it. 
	 * This is not the most effecient way to do, but it is the most configurable way I can think. 
	 * @param objPrint
	 **---------------------------------------------------------------------**/
	private void printDTD(PrintStream objPrint) {

	    //---------------------------------------------------------
		// finding and opeing DTD file (quite painful for jar file
	    //---------------------------------------------------------
		
		String hpc_dir = System.getProperty("HPCVIEWER_DIR_PATH");
		if (hpc_dir == null) return;
		
		InputStream objFile = null;
		File file = new File(hpc_dir + DTD_FILE_NAME);
		try {
			objFile = new FileInputStream(file);
	        
			readDTD(objPrint, objFile);
	        
	        objFile.close();
		} catch (IOException e) {
			// we don't need DTD. let's exit silently
			return;
		}
	}
	
	private void readDTD(PrintStream objPrint, InputStream objFile) {

	    byte[] buf=new byte[MAX_BUFFER];
	    
	    //---------------------------------------------------------
	    // iteratively read DTD file and print partially to the stream
	    //---------------------------------------------------------
        while (true) {
            int numRead = 0;
			try {
				numRead = objFile.read(buf, 0, buf.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
            if (numRead <= 0) {
                break;
            } else {
                String dtd = new String(buf, 0, numRead);
                objPrint.print(dtd);
            }

        }
	    //---------------------------------------------------------
        // DTD has been printed, we need a new line to make nice format 
	    //---------------------------------------------------------
        objPrint.println();		
	}
}
