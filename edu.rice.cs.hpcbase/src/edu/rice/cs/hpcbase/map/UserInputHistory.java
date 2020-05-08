package edu.rice.cs.hpcbase.map;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;


/*****
 * 
 * class to handle the history of user inputs (such as metric names and formula)
 * the class will store the data into Eclipse's workspace which should be
 * writable to each user
 *
 */
public class UserInputHistory {
	private static final String HISTORY_NAME_BASE = "history."; //$NON-NLS-1$
	private final static String ENCODING = "UTF-8";
    
	// temporary revert back to use deprecated code in order to keep backward compatibility
	// Original code:
	//     private static final Preferences CONFIGURATION = ConfigurationScope.INSTANCE.getNode("edu.rice.cs.hpc");   
    //private static final Preferences CONFIGURATION = new ConfigurationScope().getNode("edu.rice.cs.hpc");
    final static String NODE_HPC = "edu.rice.cs.hpc";
    
    private static final Preferences CONFIGURATION = ConfigurationScope.INSTANCE.getNode(NODE_HPC);
    
    private String name;
    private int depth;
    private List<String> history;


    public UserInputHistory(String name) {
        this(name, 50);
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
    
    public String []getHistory() {
        return (String [])this.history.toArray(new String[this.history.size()]);
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
    static public Preferences getPreference(String node) {
    	return CONFIGURATION.node(node);
    }
    
    /****
     * force to store a preference
     * @param pref
     */
    static public void setPreference( Preferences pref ) {
		// Forces the application to save the preferences
		try {
			pref.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}

    }
    
    protected void loadHistoryLines() {
        this.history = new ArrayList<String>();
        String historyData = getPreference(HISTORY_NAME_BASE).get(this.name, ""); 

        if (historyData != null && historyData.length() > 0) {
            String []historyArray = historyData.split(";"); //$NON-NLS-1$
            for (int i = 0; i < historyArray.length; i++) {
            	try {
            		historyArray[i] = new String(historyArray[i].getBytes(UserInputHistory.ENCODING), UserInputHistory.ENCODING);
                } catch (UnsupportedEncodingException e) {
                	historyArray[i] = new String(historyArray[i].getBytes());
                }
            }
            this.history.addAll(Arrays.asList(historyArray));
        }
    }
    
    protected void saveHistoryLines() {
        String result = ""; //$NON-NLS-1$
        for (Iterator<String> it = this.history.iterator(); it.hasNext(); ) {
            String str = (String)it.next();
            try {
				str = new String(str.getBytes(UserInputHistory.ENCODING), UserInputHistory.ENCODING);
			} catch (UnsupportedEncodingException e) {
				str = new String(str.getBytes());
			}
            result += result.length() == 0 ? str : (";" + str); //$NON-NLS-1$
        }
        Preferences pref = getPreference(HISTORY_NAME_BASE);
        pref.put(this.name, result);
        setPreference( pref );
    }
}
