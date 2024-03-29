package edu.rice.cs.hpcviewer.ui.graph;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.custom.CTabItem;

import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcviewer.ui.ProfilePart;


/****
 * 
 * Class to handle metric graph menus (plot, sorted and histo)
 *
 */
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
	 * @throws Exception 
	 */
	public static void createAdditionalContextMenu(
			ProfilePart profilePart,
			IMenuManager mgr, 
			IMetricManager experiment, 
			IThreadDataCollection threadData, 
			Scope scope) throws Exception {
		
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
		        	GraphEditorInput objInput = new GraphEditorInput(threadData, scope, rawMetric.get(), type);

					// display the menu
					
					Action action = new ScopeGraphAction(objInput, profilePart);
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
		private final ProfilePart profilePart;
		
		public ScopeGraphAction(
				GraphEditorInput input, 
				ProfilePart profilePart) {
			
			super(input.getGraphType());
			
			this.input  	  = input;
			this.profilePart  = profilePart;
		}
    	
		@Override
		public void run() {
			final CTabItem editor = profilePart.addEditor(input);
			editor.getDisplay().asyncExec(() -> {
				editor.getParent().setFocus();
				profilePart.onFocus();
			});
		}
    }
}
