// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

/**
 * Experiment File to manage the database: open, edit, fusion, ...
 */
package edu.rice.cs.hpclocal;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import edu.rice.cs.hpcdata.db.DatabaseManager;
import edu.rice.cs.hpcdata.util.Util;
import edu.rice.cs.hpcdata.util.Util.DatabaseFileFilter;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;


/**
 * This class manages to select, load and open a database directory
 * We assume that a database directory contains an database file (experiment.xml or meta.db)
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
	 * empty if the user click the "cancel" button
	 * @throws FileNotFoundException 
	 */
	private File[] getDatabaseFileList(Shell shell, String sTitle) throws FileNotFoundException {
		// preparing the dialog for selecting a directory
		DirectoryDialog dirDlg = new DirectoryDialog(shell);
		dirDlg.setText("hpcviewer");
		dirDlg.setFilterPath(ViewerPreferenceManager.getLastPath());		// recover the last opened path
		dirDlg.setMessage(sTitle);
		
		String sDir = dirDlg.open();	// ask the user to select a directory
		if(sDir != null) {
			checkDirectory(sDir);
			var files = getListOfDatabaseFiles(sDir);
			if (files == null || files.length == 0)
				throw new FileNotFoundException(sDir + " is not a valid database directory");
			
			return files;
		}
		return new File[0];
	}
		
	
	/**
	 * Attempt to open an experiment database if valid then
	 * open the scope view  
	 * 
	 * @return {@code String} the directory of the database if everything is OK. 
	 * 	{@code null} otherwise
	 * @throws FileNotFoundException 
	 */
	public String openFileExperiment(Shell shell) throws FileNotFoundException {
		File []files = getDatabaseFileList(shell, 
				"Select a directory containing a profiling database.");
		if(files.length > 0) {
			return getFileExperimentFromListOfFiles(files);
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
	 */
	private String getFileExperimentFromListOfFiles(File []files) {
		if((files != null) && (files.length>0)) {
			// let's make it complicated: assuming there are more than 1 XML file in this directory,
			// we need to test one by one if it is a valid database file.
			// Problem: if in the directory it has two XML files, then the second one will NEVER be opened !
			for(int i=0;i<(files.length);i++) 
			{
				File objFile = files[i];

				// Since rel 5.x, the name of database is experiment.xml
				// there is no need to maintain compatibility with hpctoolkit prior 5.x 
				// 	where the name of database is config.xml
				if (DatabaseManager.isDatabaseFile(objFile.getName()) &&  (objFile.canRead())) {
					return objFile.getAbsolutePath();
				}
			}
			throw new IllegalAccessError("Cannot find database in the directory");
		}
		// no experiment.xml in the directory
		// either the user mistakenly open a measurement directory, or it's just a mistake
		throw new IllegalAccessError("Either the selected directory is not a database or the file is corrupted.");
	}
	
	
	public static boolean checkDirectory(String path) {
		if (path == null) {
			return false;
		}
		File dir = new File(path);
		FilenameFilter filter = new Util.FileHpcrunFilter();
		File []hpcrunFiles = dir.listFiles(filter);
		if (hpcrunFiles != null && hpcrunFiles.length>0) {
			throw new IllegalArgumentException( "The folder is a measurement direcory.\n" +
							     "You need to run hpcprof to generate a database");
		}
		return true;
	}
	
	/**
	 * Return the list of .xml or meta.db files in a directory
	 * @param sPath: the directory of the database
	 * @return
	 */
	private File[] getListOfDatabaseFiles(String sPath) {
		// find XML files in this directory
		File directory = new File(sPath);
		// for debugging purpose, let have separate variable
		File[] dbFiles = directory.listFiles(new DatabaseFileFilter());

		// store the current path in the preference
		ViewerPreferenceManager.setLastPath(sPath);
		
		return dbFiles;
	}
}
