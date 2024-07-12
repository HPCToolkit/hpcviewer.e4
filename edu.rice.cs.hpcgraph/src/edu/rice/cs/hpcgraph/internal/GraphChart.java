// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcgraph.internal;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.extensions.charts.*;

import edu.rice.cs.hpcbase.ThreadViewInput;
import edu.rice.cs.hpcbase.ui.IProfilePart;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcgraph.GraphEditorInput;

/********************************************************************************
 * 
 * A customized extension of InteractiveChart for intercepting and interpreting
 * mouse click
 *
 ********************************************************************************/
public class GraphChart extends InteractiveChart implements MouseMoveListener, DisposeListener, SelectionListener
{
	public static final DecimalFormat METRIC_FORMAT = new DecimalFormat("0.0##E0##");
	
	private final IProfilePart profilePart;
	
	private MenuItem displayMenu ;
	private DefaultToolTip tooltip;
	
	private GraphEditorInput input;
	private IGraphTranslator translator;
	
	public GraphChart(IProfilePart profilePart, Composite parent, int style) {
		super(parent, style);
		
		this.profilePart = profilePart;
		
		tooltip = new DefaultToolTip(parent);
		tooltip.activate();

		getPlotArea().addMouseMoveListener(this);
		
		if ( getPlotArea() instanceof Control) {
			Control area = (Control) getPlotArea();
			var parentMenu = area.getMenu();
			
			new MenuItem(parentMenu, SWT.SEPARATOR);
			
			displayMenu = new MenuItem(parentMenu, SWT.PUSH);
			displayMenu.setEnabled(false);
			displayMenu.addSelectionListener(this);
		}		
		addDisposeListener(this);
	}

    
    public void setInput(GraphEditorInput input, IGraphTranslator translator) {
    	this.translator = translator;
    	this.input = input;
    }
    
    
	@Override
	public void mouseMove(MouseEvent e) {
		var index = getDataIndex(e);
		if (index >= 0) {
			var series  = seriesSet.getSeries()[0];
			var ySeries = series.getYSeries();
			setTooltip(index, ySeries);
			tooltip.show( new Point(e.x + 1, e.y + 1));
		} else {
			tooltip.setText("");
			displayMenu.setEnabled(false);
		}
	}
	
	
	private void setTooltip(int index, double[] ySeries) {
		var translatedIndex = translator.getIndexTranslator(index);
		
    	var idTupleType = input.getScope().getExperiment().getIdTupleType();
    	var threadData  = input.getThreadData();

		var listOfIdTuples  = threadData.getIdTupleListWithoutGPU(idTupleType);
		var idt = listOfIdTuples.get(translatedIndex);
		
		String idtLabel = idt.toString(idTupleType);

		var formattedValue = METRIC_FORMAT.format(ySeries[index]);
		var output = String.format("%s: %s", idtLabel, formattedValue);

		displayMenu.setText("Display " + idtLabel);
		displayMenu.setData(idt);
		displayMenu.setEnabled(true);
		
		tooltip.setText(output);
	}

	
	private int getDataIndex(MouseEvent e) {
		var seriesSet = getSeriesSet();
		
		// check for empty plot (no metrics)
		if (seriesSet == null || 
			seriesSet.getSeries() == null || 
			seriesSet.getSeries().length == 0)
			return -1;
		
		var series  = seriesSet.getSeries()[0];
		
		// at the moment tooltip on the historgram graph is not supported
		// only plot graph has tooltip (temporarily)
		if (!(series instanceof ILineSeries))
			return -1;
		
		var xSeries = series.getXSeries();
		
		var symSize = ((ILineSeries<?>)series).getSymbolSize();
		
		var axisSet = getAxisSet();
		var xAxis = axisSet.getXAxes()[0];
		var x = xAxis.getDataCoordinate(e.x);
		
		var xIndex = Arrays.binarySearch(xSeries, x);

		if (xIndex < 0) {
			// in case the sorting can't find the EXACT value,
			// we start from the previous x index
			xIndex = Math.min(-1 * xIndex - 1, xSeries.length-1);
		}
		int xDistance = 0;
		int maxIndex = xIndex;
		while(xDistance <= symSize && maxIndex < xSeries.length) {
			var p = series.getPixelCoordinates(maxIndex);
			xDistance = Math.abs(p.x - e.x);
			
			double distance = Math.sqrt(Math.pow(e.x - p.x, 2) + Math.pow(e.y - p.y, 2));
			if (distance <= symSize) {
				return maxIndex;
			}
			maxIndex++;
		}
		return -1;
	}


	@Override
	public void widgetDisposed(DisposeEvent e) {
		if (tooltip != null) {
			tooltip.deactivate();
			tooltip = null;
		}
		if (displayMenu != null) {
			// the parent already dispose the menu
			// need to make sure we set to null to allow GC to reclaim the memory
			displayMenu = null;
		}
	}


	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.widget.getData() instanceof IdTuple && input != null) {
			var list = new ArrayList<IdTuple>(1);
			list.add((IdTuple) e.widget.getData());
			
			ThreadViewInput viewInput = new ThreadViewInput(input.getScope().getRootScope(), 
															input.getThreadData(), 
															list);
			profilePart.addThreadView(viewInput);
		}
	}


	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// nothing
	}
}
