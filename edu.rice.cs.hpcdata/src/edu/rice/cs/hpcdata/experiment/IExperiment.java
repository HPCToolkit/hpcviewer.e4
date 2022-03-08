package edu.rice.cs.hpcdata.experiment;

import java.util.List;

import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.scope.ITreeNode;
import edu.rice.cs.hpcdata.experiment.scope.Scope;


/**********************************
 * 
 * Main interface for a database (a.k.a experiment).
 * A database can be local, remote, dense, sparse or binary.
 * It's all depend on the implementation how to represent the database.
 *
 **********************************/
public interface IExperiment {

	/****
	 * Retrieve the name or title of the database
	 * @return
	 */
	public String getName();
	
	/*****
	 * Create a tree node object of {@code ITtreeNode} based on the value.
	 * The implementer can ignore the value. 
	 * 
	 * @param value 
	 * 			Any value represents the node to be created. This can be
	 * 			an index of even null value.
	 * @return {@code ITreeNode}
	 * 			A tree node
	 */
	public ITreeNode<Scope> createTreeNode(Object value);
	
	/****
	 * Set the main root for this database. 
	 * The root usually contains three sub-roots: top-down, bottom-up and flat roots.
	 * 
	 * @param rootScope
	 * 			The main root.
	 */
	public void setRootScope(Scope rootScope);
	
	/*****
	 * Retrieve the main root of this database
	 * @return
	 */
	public Scope getRootScope();
	

	/****
	 * Set a thread data object
	 * 
	 * @param data_file
	 */
	public void setThreadData(IThreadDataCollection data_file);

	/***
	 * Return the IThreadDataCollection of this root if exists.
	 * 
	 * @return
	 */
	public IThreadDataCollection getThreadData();
	
	
	/*******
	 * Retrieve the sub-roots of this database
	 * @return
	 * 		{@code List} of sub-roots
	 */
	public List<?> getRootScopeChildren();
	
	/****
	 * Clone this database.
	 * Unlike Java's {@code clone} method, this method is public thus can be called by anyone
	 * @return
	 */
	public IExperiment duplicate();

	
	/****
	 * Retrieve the major version of the database.
	 * @return int
	 */
	public int getMajorVersion();

	/****
	 * Get the absolute path of the database
	 * @return
	 */
	public String getPath();
}
