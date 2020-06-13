package edu.rice.cs.hpcviewer.ui.graph;


/**
 * 
 * Interface callback for user selection
 *
 */
public interface IChartSelectionListener 
{
	/***
	 * Callback to be invoked when a user select or click
	 * in a chart
	 * 
	 * @param data
	 */
	public void selection(UserSelectionData data);
}
