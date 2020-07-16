 
package edu.rice.cs.hpctraceviewer.ui.main;

import javax.inject.Inject;

import javax.annotation.PostConstruct;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpctraceviewer.data.AbstractDBOpener;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.local.LocalDBOpener;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimelineService;
import edu.rice.cs.hpctraceviewer.data.util.Constants;

import javax.annotation.PreDestroy;

public class HPCTraceView 
{
	public static final String ID_WINDOW      = "edu.rice.cs.hpctraceviewer.ui.trimmedwindow.hpctraceviewer";
	public static final String ID_PERSPECTIVE = "edu.rice.cs.hpctraceviewer.ui.perspective.main";
	public static final String ID_PART        = "edu.rice.cs.hpctraceviewer.ui.part.main";
	
	public static final String ID_DATA = "hpctraceviewer.data";
	
	public static final int Y_AXIS_WIDTH  = 13;
	public static final int X_AXIS_HEIGHT = 20;
	
	private final ProcessTimelineService timelineService;
	
	private TimeAxisCanvas axisArea = null;
	private ThreadAxisCanvas processCanvas = null;
	
	/** Paints and displays the detail view.*/
	private SpaceTimeDetailCanvas detailCanvas;
	
	private IEclipseContext context;

	@Inject
	public HPCTraceView() {
		timelineService = new ProcessTimelineService();
	}
	
	@PostConstruct
	public void postConstruct(Composite parent, MWindow window) {
		
		context = window.getContext();
		context.set(Constants.CONTEXT_TIMELINE, timelineService);
		
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
		
		processCanvas = new ThreadAxisCanvas(timelineService, plotArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, true).
						hint(Y_AXIS_WIDTH, 500).applyTo(processCanvas);

		
		detailCanvas = new SpaceTimeDetailCanvas(context, plotArea); 

		detailCanvas.setLabels(labelGroup);
		
		GridDataFactory.fillDefaults().grab(true, true).
						hint(500, 500).applyTo(detailCanvas);
		
		detailCanvas.setVisible(false);
		
		
		/*************************************************************************
		 * Horizontal axis label 
		 *************************************************************************/

		Canvas footerCanvas = new Canvas(plotArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).
						hint(Y_AXIS_WIDTH, X_AXIS_HEIGHT).applyTo(footerCanvas);


		axisArea = new TimeAxisCanvas(plotArea, SWT.NO_BACKGROUND);
		GridDataFactory.fillDefaults().grab(true, false).
						hint(500, X_AXIS_HEIGHT).applyTo(axisArea);

		GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(plotArea);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(plotArea);
		
		/*************************************************************************
		 * Master layout 
		 *************************************************************************/
		
		GridLayoutFactory.fillDefaults().numColumns(1).generateLayout(parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
	}
	
	
	public ITraceViewAction getActions() {
		return detailCanvas;
	}
	
	
	@PreDestroy
	public void preDestroy() {		
	}
	
	public void setInput(BaseExperiment experiment) throws Exception {
		AbstractDBOpener dbOpener = new LocalDBOpener(context, experiment);
		SpaceTimeDataController stdc = dbOpener.openDBAndCreateSTDC(null);
		
		detailCanvas.updateView(stdc);
		axisArea.setData(stdc);
		processCanvas.setData(stdc);
	}
	
}