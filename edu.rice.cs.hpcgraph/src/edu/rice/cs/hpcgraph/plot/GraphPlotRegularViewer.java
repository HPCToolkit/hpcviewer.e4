// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcgraph.plot;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swtchart.IAxisSet;
import org.eclipse.swtchart.IAxisTick;
import org.eclipse.swtchart.ILineSeries;

import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcgraph.GraphEditorInput;
import edu.rice.cs.hpcgraph.internal.IGraphTranslator;
import edu.rice.cs.hpcgraph.internal.IdentityGraphTranlator;


/********************************************************************
 * 
 * Class to plot a graph
 *
 *********************************************************************/
public class GraphPlotRegularViewer extends AbstractGraphPlotViewer 
{
	public GraphPlotRegularViewer(CTabFolder tabFolder, int style) {
		super(tabFolder, style);
	}

	public static final String LABEL = "Plot graph";

	@Override
	protected String getXAxisTitle() {
		IAxisSet axisSet = this.getChart().getAxisSet();
		IAxisTick xTick  = axisSet.getXAxis(0).getTick();

		xTick.setFormat(new DecimalFormat("##########"));

		return "Execution context sorted by index";
	}

	@Override
	protected double[] getValuesX(Scope scope, BaseMetric metric) throws NumberFormatException, IOException {
		IThreadDataCollection threadData = getInput().getThreadData();
		return threadData.getEvenlySparseRankLabels();
	}
	
	
	@Override
	protected int setupXAxis(GraphEditorInput input, ILineSeries<?> scatterSeries) {
		try {
			Scope scope = input.getScope();
			BaseMetric metric = input.getMetric();

			double [] valuesX = getValuesX(scope, metric);
			scatterSeries.setXSeries(valuesX);
			
		} catch (NumberFormatException | IOException e) {
			showErrorMessage(e);
			return PLOT_ERR_UNKNOWN;
		}
		return PLOT_OK;
	}

	@Override
	protected double[] getValuesY(Scope scope, BaseMetric metric) throws Exception {
		
		IThreadDataCollection threadData = getInput().getThreadData();
		return threadData.getMetrics(scope, metric);
	}

	@Override
	protected ArrayList<Integer> translateUserSelection(
			ArrayList<Integer> selections) {
		return selections;
	}
	


	@Override
	protected String getGraphTypeLabel() {
		return LABEL;
	}

	@Override
	protected IGraphTranslator getGraphTranslator() {
		return new IdentityGraphTranlator();
	}
}
