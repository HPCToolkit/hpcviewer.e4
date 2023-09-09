package edu.rice.cs.hpcviewer.ui.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

import edu.rice.cs.hpcbase.IDatabase;

public class DatabaseWindowManager 
{
	/***
	 * The map between an Eclipse window to the list of databases.
	 * Note that each database is unique for each window. 
	 */
	private final HashMap<MWindow, Set<IDatabase>>   mapWindowToExperiments;


	public DatabaseWindowManager() {
		mapWindowToExperiments = new HashMap<>(1);
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
	public IDatabase getDatabase(MWindow window, String databaseId) {
		var list = getActiveListExperiments(window);

		if (list.isEmpty())
			return null;

		for (var data: list) {
			if (data.getId().equals(databaseId)) {
				return data;
			}
		}
		return null;
	}
	
	
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
		
		// find the existing database if any
		var existingDb = getDatabase(window, database.getId());
		if (existingDb != null) {
			list.remove(existingDb);
		}
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
			return Collections.emptySet();
		}
		return mapWindowToExperiments.computeIfAbsent(window, key -> new HashSet<>());
	}
	

}
