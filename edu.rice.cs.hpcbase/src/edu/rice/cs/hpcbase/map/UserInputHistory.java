// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcbase.map;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.LoggerFactory;


/*****
 * 
 * class to handle the history of user inputs (such as metric names and formula)
 * the class will store the data into Eclipse's workspace which should be
 * writable to each user
 *
 */
public class UserInputHistory 
{
	private static final int MAX_HISTORY_DEPTH = 30;
	
	private static final String HISTORY_NAME_BASE = "history."; //$NON-NLS-1$
	private static final String NODE_HPC = "edu.rice.cs.hpc";
    
    private static final Preferences CONFIGURATION = InstanceScope.INSTANCE.getNode(NODE_HPC);
    
    private String name;
    private int depth;
    private List<String> history;


    public UserInputHistory(String name) {
        this(name, MAX_HISTORY_DEPTH);
    }

    public UserInputHistory(String name, int depth) {
        this.name = name;
        this.depth = depth;
        
        this.loadHistoryLines();
    }
    
    public String getName() {
        return this.name;
    }
    
    public int getDepth() {
        return this.depth;
    }
    
    public List<String> getHistory() {
        return history;
    }
    
    public void addLine(String line) {
        if (line == null || line.trim().length() == 0) {
            return;
        }
    	this.history.remove(line);
        this.history.add(0, line);
        if (this.history.size() > this.depth) {
            this.history.remove(this.history.size() - 1);
        }
        this.saveHistoryLines();
    }
    
    public void clear() {
        this.history.clear();
        this.saveHistoryLines();
    }

    /****
     * retrieve the preference of this application
     * @param node
     * @return
     */
    public static Preferences getPreference(String node) {
    	return CONFIGURATION.node(node);
    }
    
    /****
     * force to store a preference
     * @param pref
     */
    public static void setPreference( Preferences pref ) {
		// Forces the application to save the preferences
		try {
			pref.flush();
		} catch (BackingStoreException e) {
			// fail to store the preferences
			var logger = LoggerFactory.getLogger(UserInputHistory.class);
			logger.error(e.getMessage(), e);
		}
    }
    
    protected void loadHistoryLines() {
        this.history = new ArrayList<>();
        String historyData = getPreference(HISTORY_NAME_BASE).get(this.name, ""); 

        if (historyData != null && historyData.length() > 0) {
            String []historyArray = historyData.split(";"); //$NON-NLS-1$
            for (int i = 0; i < historyArray.length; i++) {
            	historyArray[i] = new String(historyArray[i].getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            }
            this.history.addAll(Arrays.asList(historyArray));
        }
    }
    
    protected void saveHistoryLines() {
        String result = ""; //$NON-NLS-1$
        for (Iterator<String> it = this.history.iterator(); it.hasNext(); ) {
            String str = it.next();
            str = new String(str.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            result += result.length() == 0 ? str : (";" + str); //$NON-NLS-1$
        }
        Preferences pref = getPreference(HISTORY_NAME_BASE);
        pref.put(this.name, result);
        setPreference( pref );
    }
}
