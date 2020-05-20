package edu.rice.cs.hpc.filter.service;


/**************************************************************
 * 
 * Class to manage filter state, whether it needs to be refreshed
 * or there's a filter selection in the view
 *
 **************************************************************/
public class FilterStateProvider 
{
	final static public String FILTER_REFRESH_PROVIDER = "edu.rice.cs.hpc.filter.update";
	final static public String FILTER_ENABLE_PROVIDER = "edu.rice.cs.hpc.filter.enable";
	
	final static public String TOGGLE_COMMAND = "org.eclipse.ui.commands.toggleState";
	final static public String SELECTED_STATE = "SELECTED";
	
	public FilterStateProvider() {
		// TODO Auto-generated constructor stub
	}

	
	
	/*****
	 * refresh the table as the filter pattern may change
	 * Usually called by FilterAdd and FilterDelete 
	 */
	public void broadcast()
	{
	}
}
