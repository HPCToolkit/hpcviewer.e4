package edu.rice.cs.hpcviewer.ui.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcbase.IDatabase;

/***********************************
 * 
 * Class to manage the database of hpcviewer windows
 * and its associated experiment databases
 *
 */
public class DatabaseWindowManager 
{
	/****
	 * Status of the existence of a database in a given window
	 * <ul>
	 *   <li> {@code INEXIST}: the given database doesn't exist
	 *   <li> {@code EXIST_CANCEL}: the given database already exists, but the user doesn't want to continue
	 *   <li> {@code EXIST_REPLACE}: the database already exists and user confirms to replace it
	 * </ul>
	 */
	public enum DatabaseExistence {INEXIST, EXIST_CANCEL, EXIST_REPLACE};
	
	/***
	 * The map between an Eclipse window to the list of databases.
	 * Note that each database is unique for each window. 
	 */
	private final HashMap<MWindow, Set<IDatabase>>   mapWindowToExperiments;


	public DatabaseWindowManager() {
		mapWindowToExperiments = new HashMap<>(1);
	}

	
	/***
	 * Verify if a given database is already opened or not for this window.
	 * 
	 * @param shell
	 * @param window
	 * @param dbId
	 * @return
	 */
	public DatabaseExistence checkAndConfirmDatabaseExistence(Shell shell, MWindow window, String dbId) {
		if (dbId == null)
			return DatabaseExistence.INEXIST;
		
		var db = getDatabase(window, dbId);
		if (db == null || db.isEmpty())
			return DatabaseExistence.INEXIST;
				
		String msg = dbId + ": The database already exists.\nDo you want to replace it?";
		if ( MessageDialog.openQuestion(shell, "Database already exists", msg) )
			return DatabaseExistence.EXIST_REPLACE;

		return DatabaseExistence.EXIST_CANCEL;
	}

	/***
	 * Retrieve the iterator of the database collection from a given window
	 * 
	 * @param window 
	 * @return Iterator for the list of the given window
	 */
	public Iterator<IDatabase> getIterator(MWindow window) {
		var list = mapWindowToExperiments.get(window);
		if (list == null)
			return Collections.emptyIterator();
		
		return list.iterator();
	}


	/***
	 * Retrieve the current registered databases
	 * @return
	 */
	public int getNumDatabase(MWindow window) {
		var list = getActiveListExperiments(window);
		return list.size();
	}


	/***
	 * Check if the database is empty or not
	 * @return true if the database is empty
	 */
	public boolean isEmpty(MWindow window) {
		var list = getActiveListExperiments(window);		
		return list.isEmpty();
	}


	/***
	 * Retrieve the experiment object given a XML file path
	 * 
	 * @param window
	 * 
	 * @param databaseId
	 * 
	 * @return IDatabase object if the database exist, null otherwise.
	 */
	public Set<IDatabase> getDatabase(MWindow window, String databaseId) {
		var list = getActiveListExperiments(window);

		if (list.isEmpty())
			return Collections.emptySet();

		var result = new HashSet<IDatabase>();
		
		for (var data: list) {
			if (data.getId().equals(databaseId)) {
				result.add(data);
			}
		}
		return result;
	}
	
	
	/****
	 * Get the set of databases of a specific window
	 * 
	 * @param window
	 * @return the set of databases or empty set. It doesn't return null
	 */
	public Set<IDatabase> getDatabaseSet(MWindow window) {
		return getActiveListExperiments(window);
	}
	
	
	/****
	 * Add a database into the list of current opened databases of a given window
	 * 
	 * @param window
	 * 			The active window
	 * @param database
	 * 			The database to be added to the list. 
	 * 			If the database already exists, it will replace it.
	 */
	public void addDatabase(MWindow window, IDatabase database) {
		var list = getActiveListExperiments(window);

		// naively add the database even if the list has the same 
		// database id with different database object
		list.add(database);
	}
	
	
	public void removeDatabase(MWindow window, IDatabase database) {
		var list = getActiveListExperiments(window);
		
		if (list != null && !list.isEmpty()) {
			list.remove(database);
			database.close();
		}
	}
	
	/****
	 * Remove a window 
	 * @param window
	 * @return
	 */
	public Set<IDatabase> removeWindow(MWindow window) {
		return mapWindowToExperiments.remove(window);
	}
	
	
	/***
	 * Retrieve the list of experiments of the current window.
	 * If Eclipse reports there is no active window, the list is null.
	 * 
	 * @return the list of experiments (if there's an active window). 
	 * 		   empty list otherwise.
	 * 
	 */
	private Set<IDatabase> getActiveListExperiments(MWindow window) {

		if (window == null) {
			return new HashSet<>();
		}
		return mapWindowToExperiments.computeIfAbsent(window, key -> new HashSet<>());
	}
	

}
