/**
 * Experiment File to manage the database: open, edit, fusion, ...
 */
package edu.rice.cs.hpcviewer.ui.experiment;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import java.io.File;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpc.data.util.Util.FileXMLFilter;
import edu.rice.cs.hpcviewer.ui.preferences.PreferenceManager;


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
	 * Constructor to instantiate experiment file
	 * @param win: the current workbench window
	 */
	public ExperimentManager() {
	}
	

	
	/**
	 * Get the list of database file name
	 * @param shell : the shell widget of the application
	 * @param sTitle : the title of the window 
	 * 
	 * @return the list of XML files in the selected directory
	 * null if the user click the "cancel" button
	 */
	private File[] getDatabaseFileList(Shell shell, String sTitle) {
		// preparing the dialog for selecting a directory
		Shell objShell = shell;
		DirectoryDialog dirDlg = new DirectoryDialog(objShell);
		dirDlg.setText("hpcviewer");
		dirDlg.setFilterPath(PreferenceManager.getLastPath());		// recover the last opened path
		dirDlg.setMessage(sTitle);
		String sDir = dirDlg.open();	// ask the user to select a directory
		if(sDir != null){
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
	 */
	public BaseExperiment openDatabaseFromDirectory(Shell shell, String sPath) {
		File []fileXML = this.getListOfXMLFiles(sPath);
		if(fileXML != null)
			return openFileExperimentFromFiles(shell, fileXML);
		return null;
	}
	/**
	 * Attempt to open an experiment database if valid then
	 * open the scope view  
	 * @return true if everything is OK. false otherwise
	 */
	public BaseExperiment openFileExperiment(Shell shell) {
		File []fileXML = getDatabaseFileList(shell, 
				"Select a directory containing a profiling database.");
		if(fileXML != null)
			return openFileExperimentFromFiles(shell, fileXML);
		return null;
	}

	//==================================================================
	// ---------- PRIVATE PART-----------------------------------------
	//==================================================================
	/**
	 * Open an experiment database based on given an array of java.lang.File
	 * @param filesXML: list of files
	 * @return true if the opening is successful
	 */
	private BaseExperiment openFileExperimentFromFiles(Shell shell, File []filesXML) {
		if((filesXML != null) && (filesXML.length>0)) {
			boolean bContinue = true;
			// let's make it complicated: assuming there are more than 1 XML file in this directory,
			// we need to test one by one if it is a valid database file.
			// Problem: if in the directory it has two XML files, then the second one will NEVER be opened !
			for(int i=0;i<(filesXML.length) && (bContinue);i++) 
			{
				File objFile = filesXML[i];
				String sFile=objFile.getAbsolutePath();

				// Since rel 5.x, the name of database is experiment.xml
				// there is no need to maintain compatibility with hpctoolkit prior 5.x 
				// 	where the name of database is config.xml
				if(objFile.getName().startsWith(Constants.DATABASE_FILENAME))  
				{
					// ------------------------------------------------------------------
					// we will continue to verify the content of the list of XML files
					// until we fine the good one.
					// ------------------------------------------------------------------

					// check if we can open the database successfully
					BaseExperiment exp = loadExperiment(shell, sFile);
					if (exp != null)
						return exp;
				}
			}
			return null;
		}
		MessageDialog.openError(shell, "Failed to open a database", 
			"Either the selected directory is not a database or the max number of databases allowed per window are already opened.\n"+
			"A database directory must contain at least one XML file which contains profiling information.");
		return null;
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
		File filesXML[] = files.listFiles(new FileXMLFilter());

		// store the current path in the preference
		PreferenceManager.setLastPath(sPath);
		
		return filesXML;
	}
	

	private BaseExperiment loadExperiment(Shell shell, String sFilename) {
		Experiment experiment = null;
		try
		{
			experiment = new Experiment();
			experiment.open( new java.io.File(sFilename), new edu.rice.cs.hpcbase.map.ProcedureAliasMap(), true );

		} catch(java.io.FileNotFoundException fnf)
		{
			System.err.println("File not found:" + sFilename + "\tException:"+fnf.getMessage());
			MessageDialog.openError(shell, "Error:File not found", "Cannot find the file "+sFilename);
			experiment = null;
		}
		catch(java.io.IOException io)
		{
			System.err.println("IO error:" +  sFilename + "\tIO msg: " + io.getMessage());
			MessageDialog.openError(shell, "Error: Unable to read", "Cannot read the file "+sFilename);
			experiment = null;
		}
		catch(InvalExperimentException ex)
		{
			String where = sFilename + " " + " " + ex.getLineNumber();
			System.err.println("$" +  where);
			MessageDialog.openError(shell, "Incorrect Experiment File", "File "+sFilename 
					+ " has incorrect tag at line:"+ex.getLineNumber());
			experiment = null;
		} 
		catch(NullPointerException npe)
		{
			System.err.println("$" + npe.getMessage() + sFilename);
			MessageDialog.openError(shell, "File is invalid", "File has null pointer:"
					+sFilename + ":"+npe.getMessage());
			experiment = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return experiment;

	}
}
