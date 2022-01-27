/**
 * Experiment File to manage the database: open, edit, fusion, ...
 */
package edu.rice.cs.hpcviewer.ui.experiment;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import java.io.File;
import java.io.FilenameFilter;
import edu.rice.cs.hpcbase.map.ProcedureAliasMap;
import edu.rice.cs.hpcdata.db.DatabaseManager;
import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpcdata.util.Util;
import edu.rice.cs.hpcdata.util.Util.DatabaseFileFilter;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;


/**
 * This class manages to select, load and open a database directory
 * We assume that a database directory contains an XML file (i.e. extension .xml)
 * Warning: This class is not compatible with the old version of experiment file 
 *  (the old version has no xml extension)
 *
 */
public class ExperimentManager 
{		
	/**
	 * Get the list of database file name
	 * @param shell : the shell widget of the application
	 * @param sTitle : the title of the window 
	 * 
	 * @return the list of XML files in the selected directory
	 * null if the user click the "cancel" button
	 * @throws Exception 
	 */
	private File[] getDatabaseFileList(Shell shell, String sTitle) throws Exception {
		// preparing the dialog for selecting a directory
		DirectoryDialog dirDlg = new DirectoryDialog(shell);
		dirDlg.setText("hpcviewer");
		dirDlg.setFilterPath(ViewerPreferenceManager.getLastPath());		// recover the last opened path
		dirDlg.setMessage(sTitle);
		String sDir = dirDlg.open();	// ask the user to select a directory
		if(sDir != null && checkDirectory(sDir)) {
			return this.getListOfXMLFiles(sDir);
		}
		
		return null;
	}
	
	/**
	 * Open a database given a path to the database directory
	 * @param sPath : absolute path of the database directory
	 * @param flag : whether to show callers view or not
	 * 
	 * @return true if the database has been successfully opened
	 * @throws Exception 
	 */
	public BaseExperiment openDatabaseFromDirectory(Shell shell, String sPath) throws Exception {
		if (!checkDirectory(sPath))
			return null;
		
		File []fileXML = this.getListOfXMLFiles(sPath);
		if (fileXML == null)
			return null;
		
		String filename = getFileExperimentFromListOfFiles(fileXML);
		if (filename != null) {
			return loadExperiment(filename);
		}
		return null;
	}
	
	
	/**
	 * Attempt to open an experiment database if valid then
	 * open the scope view  
	 * @return true if everything is OK. false otherwise
	 * @throws Exception 
	 */
	public String openFileExperiment(Shell shell) throws Exception {
		File []fileXML = getDatabaseFileList(shell, 
				"Select a directory containing a profiling database.");
		if(fileXML != null) {
			return getFileExperimentFromListOfFiles(fileXML);
		}
		return null;
	}
	

	//==================================================================
	// ---------- PRIVATE PART-----------------------------------------
	//==================================================================
	/**
	 * Open an experiment database based on given an array of java.lang.File
	 * @param filesXML: list of files
	 * @return true if the opening is successful
	 * @throws Exception 
	 */
	private String getFileExperimentFromListOfFiles(File []filesXML) throws Exception {
		if((filesXML != null) && (filesXML.length>0)) {
			// let's make it complicated: assuming there are more than 1 XML file in this directory,
			// we need to test one by one if it is a valid database file.
			// Problem: if in the directory it has two XML files, then the second one will NEVER be opened !
			for(int i=0;i<(filesXML.length);i++) 
			{
				File objFile = filesXML[i];

				// Since rel 5.x, the name of database is experiment.xml
				// there is no need to maintain compatibility with hpctoolkit prior 5.x 
				// 	where the name of database is config.xml
				if (DatabaseManager.isDatabaseFile(objFile.getName())) {
					if (objFile.canRead())
						return objFile.getAbsolutePath();
					
				}
			}
			throw new Exception("Cannot find experiment.xml in the directory");
		}
		// no experiment.xml in the directory
		// either the user mistakenly open a measurement directory, or it's just a mistake
		throw new Exception("Either the selected directory is not a database or the file is corrupted.\n"+
				"A database directory must contain at least one XML file which contains profiling information.");
	}
	
	private boolean checkDirectory(String path) throws Exception {
		if (path == null) {
			return false;
		}
		File dir = new File(path);
		FilenameFilter filter = new Util.FileHpcrunFilter();
		File []hpcrunFiles = dir.listFiles(filter);
		if (hpcrunFiles != null && hpcrunFiles.length>0) {
			throw new Exception( "The folder is a measurement direcory.\n" +
							     "You need to run hpcprof to generate a database");
		}
		return true;
	}
	
	/**
	 * Return the list of .xml files in a directory
	 * @param sPath: the directory of the database
	 * @return
	 */
	private File[] getListOfXMLFiles(String sPath) {
		// find XML files in this directory
		File files = new File(sPath);
		// for debugging purpose, let have separate variable
		File filesXML[] = files.listFiles(new DatabaseFileFilter());

		// store the current path in the preference
		ViewerPreferenceManager.setLastPath(sPath);
		
		return filesXML;
	}
	

	/****
	 * Open and load the experiment.xml file, then return an instance of experiment object 
	 * @param shell
	 * @param sFilename
	 * @return
	 * @throws Exception
	 */
	public BaseExperiment loadExperiment(final String sFilename) throws Exception {
		Experiment experiment = new Experiment();
		experiment.open( new File(sFilename), new ProcedureAliasMap(), true );

		return experiment;
	}
}
