 
package edu.rice.cs.hpcviewer.ui.graph;

import javax.inject.Inject;

import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.Chart;
import org.swtchart.IAxisSet;
import org.swtchart.IAxisTick;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpcviewer.ui.parts.editor.IUpperPart;

import javax.annotation.PreDestroy;
import org.eclipse.e4.ui.di.Focus;

public abstract class GraphViewer implements IUpperPart
{
	static public final int MAX_TITLE_CHARS = 100; // maximum charaters for a title
	
    private Chart chart;
    private GraphEditorInput input;
    

	@Inject
	public GraphViewer() {		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
		// set the window title with a possible db number

		//----------------------------------------------
		// chart creation
		//----------------------------------------------
		chart = new GraphChart(parent, SWT.NONE);


		//----------------------------------------------
		// formatting axis
		//----------------------------------------------
		IAxisSet axisSet = chart.getAxisSet();
		IAxisTick yTick = axisSet.getYAxis(0).getTick();
		yTick.setFormat(new DecimalFormat("0.0##E0##"));

		// turn off the legend
		chart.getLegend().setVisible(false);		
	}
	
	
	@PreDestroy
	public void preDestroy() {
	}
	
	
	@Focus
	public void onFocus() {
		chart.setFocus();
	}

	@Override
	public BaseExperiment getExperiment() {
		return input.getScope().getExperiment();
	}

	@Override
	public String getTitle() {
		return getTitle(getPartDescriptorId(), input.getScope(), input.getMetric());
	}


	@Override
	public void setMarker(int lineNumber) {
	}

	@Override
	public void display(Object obj) {

		if (obj == null) return;
		
		input = (GraphEditorInput) obj;
		
		chart.getTitle().setText(getTitle());
		
		//----------------------------------------------
		// formatting axis
		//----------------------------------------------
		IAxisSet axisSet = chart.getAxisSet();
		IAxisTick yTick = axisSet.getYAxis(0).getTick();
		yTick.setFormat(new DecimalFormat("0.0##E0##"));

		// turn off the legend
		chart.getLegend().setVisible(false);
		
		//----------------------------------------------
		// main part: ask the subclass to plot the graph
		//----------------------------------------------

		plotData(input);
	}
	
	protected GraphEditorInput getInput() {
		return input;
	}
	
	static public String getTitle(String descID, Scope scope, BaseMetric metric) {
		
		String scopeName = scope.getName();
		if (scopeName.length() > MAX_TITLE_CHARS) {
			scopeName = scope.getName().substring(0, MAX_TITLE_CHARS) + "...";
		}
		String type  = getTypeLabel(descID);
		String title = "[" + type + "] " + scopeName +": " + metric.getDisplayName();
		
		return title;
	}
	
	static public String getTypeLabel(String descID) {
		
		String type = null;
		if (descID.equals(GraphPlotRegularViewer.ID)) {
			type = "Plot graph";
		} else if (descID.equals(GraphPlotSortViewer.ID)) {
			type = "Sorted plot graph";
		} else if (descID.equals(GraphHistoViewer.ID)) {
			type = "Histogram graph";
		}
		return type;
	}
	
	static public String getID(String descID, Scope scope, BaseMetric metric) {
		
		int dbId     = scope.getExperiment().hashCode();
		int scopeId  = scope.getCCTIndex();
		int metricId = metric.getIndex();
		int graphId  = descID.hashCode();
		
		return String.valueOf(dbId) 	+ ":" + 
			   String.valueOf(scopeId)  + ":" +
			   String.valueOf(metricId) + ":" +
			   String.valueOf(graphId);
	}
	
	protected Chart getChart() {
		return chart;
	}
	
	/**
	 * method to plot a graph of a specific scope and metric of an experiment
	 * 
	 * @param scope: the scope to plot
	 * @param metric: the raw metric to plot
	 */
	protected abstract int plotData(GraphEditorInput input);
	
	/****
	 * Translate a set of thread-index selections into the original set of
	 * thread-index selection.<br/>
	 * It is possible that the child class change the index of x-axis. This
	 * method will then translate from the current selected index to the original
	 * index so that it can be displayed properly by {@link ThreadView}. 
	 *  
	 * @param selections : a set of selected index (usually only one item)
	 * @return the translated set of indexes
	 */
	protected abstract ArrayList<Integer> translateUserSelection(ArrayList<Integer> selections); 

	
}