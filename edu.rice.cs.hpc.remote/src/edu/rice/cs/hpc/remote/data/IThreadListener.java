package edu.rice.cs.hpc.remote.data;

/**************************************
 * 
 * Interface for listening a thread
 * 
 * - the master (listener) has to implement the interface and
 * 	 do an action based on notify method
 * 
 * - The child thread needs to call notify() once an error occurs 
 *
 */
public interface IThreadListener {
	
	/***
	 * Notify the listener that something has happened
	 * @param msg
	 */
	public void notify(String msg);
	
}
