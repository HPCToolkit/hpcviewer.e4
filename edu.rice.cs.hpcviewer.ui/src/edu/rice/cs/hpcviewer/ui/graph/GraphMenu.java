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
import edu.rice.cs.hpcviewer.ui.parts.editor.Editor;
import edu.rice.cs.hpcviewer.ui.parts.editor.PartFactory;


/****
 * 
 * Class to handle metric graph menus (plot, sorted and histo)
 *
 */
public class GraphMenu 
{
	static public void createAdditionalContextMenu(
			PartFactory partFactory,
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
			
			final int num_metrics = metrics.length;
			for (int i=0; i<num_metrics; i++) {
				
				// do not display empty metric 
				// this is important to keep consistency with the table
				// which doesn't display empty metrics
				
				RootScope root = scope.getRootScope();
				MetricValue mv = root.getMetricValue(metrics[i]);
				if (mv == MetricValue.NONE)
					continue;
				
				// display the menu
				
				MenuManager subMenu = new MenuManager("Graph "+ metrics[i].getDisplayName() );
				createGraphMenus(threadData, partFactory, subMenu, scope, metrics[i]);
				mgr.add(subMenu);
			}
		}		
	} 

	/***
	 * Create 3 submenus for plotting graph: plot, sorted and histo
	 * @param menu
	 * @param scope
	 * @param m
	 * @param index
	 */
	static private void createGraphMenus(
			IThreadDataCollection threadData, 
			PartFactory partFactory, 
			IMenuManager menu, 
			Scope scope, 
			BaseMetric m) {
		menu.add( createGraphMenu(threadData, partFactory, scope, m, GraphPlotRegularViewer.ID) );
		menu.add( createGraphMenu(threadData, partFactory, scope, m, GraphPlotSortViewer.ID) );
		menu.add( createGraphMenu(threadData, partFactory, scope, m, GraphHistoViewer.ID) );
	}
	
	/***
	 * Create a menu action for graph
	 * @param scope
	 * @param m
	 * @param index
	 * @param t
	 * @return
	 */
	static private ScopeGraphAction createGraphMenu(
			IThreadDataCollection threadData, 
			PartFactory partFactory, 
			Scope scope, 
			BaseMetric m, 
			String t) {
		
		final String sTitle = GraphViewer.getTypeLabel(t);
		return new ScopeGraphAction( threadData, partFactory, sTitle, scope, m, t);
	}
	

    /********************************************************************************
     * class to initialize an action for displaying a graph
     ********************************************************************************/
    static private class ScopeGraphAction extends Action 
    {
    	final private IThreadDataCollection threadData;
    	
    	final private String descriptorId;
    	final private BaseMetric metric;	
    	final private Scope scope;
    	final private PartFactory partFactory;
    	
		public ScopeGraphAction(
				IThreadDataCollection threadData,
				PartFactory partFactory, 
				String sTitle, 
				Scope scopeCurrent, 
				BaseMetric m, 
				String descriptorId) {
			
			super(sTitle);
			
			this.threadData  = threadData;
			this.partFactory  = partFactory;
			this.metric 	  = m;
			this.descriptorId = descriptorId;
			this.scope 		  = scopeCurrent;
		}
    	
		public void run() {
			
        	GraphEditorInput objInput = new GraphEditorInput(threadData, scope, metric);
        	String elementId = GraphViewer.getID(descriptorId, scope, metric);
        	
        	partFactory.display(Editor.STACK_ID, descriptorId, elementId, objInput);
		}
    }
}
