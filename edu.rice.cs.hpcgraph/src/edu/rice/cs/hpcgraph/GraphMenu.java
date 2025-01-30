// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcgraph;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import edu.rice.cs.hpcbase.ui.IProfilePart;
import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcgraph.histogram.GraphHistoViewer;
import edu.rice.cs.hpcgraph.plot.GraphPlotRegularViewer;
import edu.rice.cs.hpcgraph.plot.GraphPlotSortViewer;



/***********************************************************************************
 * 
 * Class to handle metric graph menus (plot, sorted and histogram)
 *
 **********************************************************************************/
public class GraphMenu 
{
	private GraphMenu() {
		// hide the constructor
	}
	
	/****
	 * Add a context menu of plot graphs on the given menu manager.
	 * The context menu only displays the non-empty metrics of the 
	 * selected (or specified) scope node.
	 * 
	 * @param profilePart
	 * 			The main profile part to show a plot graph
	 * @param mgr
	 * 			The main menu manager
	 * @param experiment
	 * 			The current experiment or metric manager (in case thread view)
	 * @param threadData
	 * 			An interface to access the raw data
	 * @param scope
	 * 			The selected node 
	 */
	public static void createAdditionalContextMenu(
			IProfilePart profilePart,
			IMenuManager mgr, 
			IMetricManager experiment, 
			IThreadDataCollection threadData, 
			Scope scope) {
		
		if (scope == null || threadData == null || !threadData.isAvailable())
			// no menus if there is no thread-level data
			return;
		
		var listOfIndexes = experiment.getNonEmptyMetricIDs(scope);
		if (listOfIndexes == null || listOfIndexes.isEmpty())
			// empty metrics: Perhaps should throw an exception
			return;
		
		mgr.add( new Separator() );
		
		final String[] graphTypes = { GraphPlotRegularViewer.LABEL,
									  GraphPlotSortViewer.LABEL,
									  GraphHistoViewer.LABEL };
		
		// fix issue #221: do not show empty metrics
		// get the list of indexes of non-empty metrics
		// if the table is empty or has no metrics, do nothing

		for(var idx: listOfIndexes) {
			var metric = experiment.getMetric(idx);
			var rawMetric = metric.getMetricRaw();
			if (rawMetric.isPresent()) {
				MenuManager subMenu = new MenuManager("Graph "+ rawMetric.get().getDisplayName() );

				for (String type: graphTypes) {
		        	GraphEditorInput objInput = new GraphEditorInput(profilePart, threadData, scope, rawMetric.get(), type);

					// display the menu
					
					Action action = new ScopeGraphAction(objInput);
					subMenu.add( action );

					mgr.add(subMenu);
				}
			}
		}
	} 
	

    /********************************************************************************
     * class to initialize an action for displaying a graph
     ********************************************************************************/
    private static class ScopeGraphAction extends Action 
    {
		private final GraphEditorInput input;
		
		public ScopeGraphAction(GraphEditorInput input) {
			
			super(input.getGraphType());
			
			this.input = input;
		}
    	
		@Override
		public void run() {
			final var profilePart = input.getProfilePart();
			final var editor = profilePart.addEditor(input);
			editor.setFocus();
		}
		
		
		/***
		 * Free allocated resources
		 */
		public void dispose() {
			if (input != null)
				input.dispose();
		}
    }
}
