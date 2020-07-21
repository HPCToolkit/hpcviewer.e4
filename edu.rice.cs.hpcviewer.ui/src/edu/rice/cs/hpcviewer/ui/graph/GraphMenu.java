package edu.rice.cs.hpcviewer.ui.graph;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpcviewer.ui.parts.ProfilePart;


/****
 * 
 * Class to handle metric graph menus (plot, sorted and histo)
 *
 */
public class GraphMenu 
{
	static public void createAdditionalContextMenu(
			ProfilePart profilePart,
			IMenuManager mgr, Experiment experiment, 
			IThreadDataCollection threadData, Scope scope) {
		
		if (scope != null) {

			if (threadData == null || !threadData.isAvailable())
				// no menus if there is no thread-level data
				return;
			
			final BaseMetric []metrics = experiment.getMetricRaw();
			if (metrics == null)
				return;
			
			mgr.add( new Separator() );
			
			final String graphTypes[] = { GraphPlotRegularViewer.LABEL,
										  GraphPlotSortViewer.LABEL,
										  GraphHistoViewer.LABEL };
			
			for (BaseMetric metric: metrics) {
				
				// do not display empty metric 
				// this is important to keep consistency with the table
				// which doesn't display empty metrics
				
				RootScope root = scope.getRootScope();
				MetricValue mv = root.getMetricValue(metric);
				if (mv == MetricValue.NONE)
					continue;

				MenuManager subMenu = new MenuManager("Graph "+ metric.getDisplayName() );

				for (String type: graphTypes) {
		        	GraphEditorInput objInput = new GraphEditorInput(threadData, scope, metric, type);

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
    static private class ScopeGraphAction extends Action 
    {
		final private GraphEditorInput input;
		final private ProfilePart profilePart;
		
		public ScopeGraphAction(
				GraphEditorInput input, 
				ProfilePart profilePart) {
			
			super(input.getGraphType());
			
			this.input  	  = input;
			this.profilePart  = profilePart;
		}
    	
		public void run() {
			profilePart.addEditor(input);
		}
    }
}
