// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.main;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.ui.base.AbstractBaseItem;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.base.ITraceViewAction;

/*****************************************************************
 * 
 * Main class for the trace view which manages the main view
 * Every thing is done in the main view will affect other views
 *
 *****************************************************************/
public class HPCTraceView extends AbstractBaseItem
{
	public static final String ID_PERSPECTIVE = "edu.rice.cs.hpctraceviewer.ui.perspective.main";
	public static final String ID_PART        = "edu.rice.cs.hpctraceviewer.ui.part.main";
	
	public static final String ID_DATA = "hpctraceviewer.data";
	
	public static final int Y_AXIS_WIDTH  = 12;
	public static final int X_AXIS_HEIGHT = 20;
		
	private CanvasAxisX canvasAxisX = null;
	private CanvasAxisY canvasAxisY = null;
	
	/** Paints and displays the detail view.*/
	private SpaceTimeDetailCanvas canvasMain;
	
	@Inject
	public HPCTraceView(CTabFolder parent, int style) {
		super(parent, style);
	}
	
	@Override
	public void createContent(ITracePart parentPart, 
							  IEclipseContext context,
							  IEventBroker eventBroker,
							  Composite parent) {
		
		/**************************************************************************
         * Process and Time dimension labels
         *************************************************************************/
		final Composite headerArea = new Composite(parent, SWT.NONE);
		
		Canvas headerCanvas = new Canvas(headerArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).
						hint(Y_AXIS_WIDTH, 20).applyTo(headerCanvas);
		
		final Composite labelGroup = new Composite(headerArea, SWT.NONE);

		GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(headerArea);
		GridDataFactory.fillDefaults().grab(true, false).
						applyTo(headerArea);

		
		/*************************************************************************
		 * Detail View Canvas
		 ************************************************************************/

		Composite plotArea = new Composite(parent, SWT.NONE);
		
		canvasAxisY = new CanvasAxisY(parentPart, plotArea);
		GridDataFactory.fillDefaults().grab(false, true).
						hint(Y_AXIS_WIDTH, 500).applyTo(canvasAxisY);

		
		canvasMain = new SpaceTimeDetailCanvas(parentPart, eventBroker, plotArea); 

		canvasMain.setLabels(labelGroup);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(canvasMain);

		canvasMain.setVisible(true);
		
		/*************************************************************************
		 * Horizontal axis label 
		 *************************************************************************/

		Canvas footerCanvas = new Canvas(plotArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).
						hint(Y_AXIS_WIDTH, X_AXIS_HEIGHT).applyTo(footerCanvas);


		canvasAxisX = new CanvasAxisX(parentPart, plotArea);
		GridDataFactory.fillDefaults().grab(true, false).
						hint(500, X_AXIS_HEIGHT).applyTo(canvasAxisX);

		GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(plotArea);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(plotArea);
		
		/*************************************************************************
		 * Master layout 
		 *************************************************************************/
		
		GridLayoutFactory.fillDefaults().numColumns(1).generateLayout(parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
	}
	
	
	/****
	 * Get the {@code ITraceViewAction} of the trace view
	 * @return
	 */
	public ITraceViewAction getActions() {
		return canvasMain;
	}
	

	/***
	 * Refresh the content of the main view and its derivatives 
	 * (depth view, summary view, etc). 
	 */
	public void refresh() {
		canvasMain.refresh(true);
	}
	
	/****
	 * Similar to refresh, without changing the structure
	 */
	public void redraw() {
		canvasMain.refresh(false);
	}
	
	
	
	/****
	 * Display a message on top of the view.
	 * The message will only appear for a couple of seconds, and then disappear.
	 * 
	 * @param message
	 */
	public void showMessage(String message) {
		canvasMain.setMessage(message);
	}

	@Override
	public void setInput(Object input) {
		SpaceTimeDataController stdc = (SpaceTimeDataController) input;
		
		canvasMain. setData(stdc);
		canvasAxisX.setData(stdc);
		canvasAxisY.setData(stdc);
	}	
}