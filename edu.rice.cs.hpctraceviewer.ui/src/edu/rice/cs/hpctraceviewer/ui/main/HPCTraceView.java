 
package edu.rice.cs.hpctraceviewer.ui.main;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import javax.annotation.PreDestroy;

public class HPCTraceView 
{
	public static final String ID_WINDOW      = "edu.rice.cs.hpctraceviewer.ui.trimmedwindow.hpctraceviewer";
	public static final String ID_PERSPECTIVE = "edu.rice.cs.hpctraceviewer.ui.perspective.main";
	
	/**The ID needed to create this view (used in plugin.xml).*/
	public static final String ID = "hpctraceview.view";
	
	public static final int Y_AXIS_WIDTH  = 13;
	public static final int X_AXIS_HEIGHT = 20;

	@Inject
	public HPCTraceView() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
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
		

		Canvas footerCanvas = new Canvas(plotArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).
						hint(Y_AXIS_WIDTH, X_AXIS_HEIGHT).applyTo(footerCanvas);

		GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(plotArea);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(plotArea);
		
		/*************************************************************************
		 * Master layout 
		 *************************************************************************/
		
		GridLayoutFactory.fillDefaults().numColumns(1).generateLayout(parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
	}
	
	
	@PreDestroy
	public void preDestroy() {
		
	}
	
	
	
}