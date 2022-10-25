package edu.rice.cs.hpcbase;

public interface BaseConstants 
{
	public enum ViewType {COLLECTIVE, INDIVIDUAL}


	/** Event when a database has to be removed from the application */
	public static final String TOPIC_HPC_REMOVE_DATABASE = "hpcviewer/database_remove";
	
    
	/**The darkest color for black over depth text (switch to white if the sum of the 
	 * R, G, and B components is less than this number).*/
	public static final int DARKEST_COLOR_FOR_BLACK_TEXT = 384;
	
}
