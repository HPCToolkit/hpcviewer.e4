package edu.rice.cs.hpctraceviewer.ui.summary;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.ui.base.AbstractBaseItem;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.main.HPCTraceView;


/*************************************************************************
 * 
 * View part of the summary window 
 *
 *************************************************************************/
public class HPCSummaryView extends AbstractBaseItem
{

	public HPCSummaryView(CTabFolder parent, int style) {
		super(parent, style);
	}

	public static final String ID = "hpcsummaryview.view";
	
	/**The canvas that actually displays this view*/
	SummaryTimeCanvas summaryCanvas;
	
	@Override
	public void createContent( ITracePart parentPart, 
							   IEclipseContext context, 
							   IEventBroker broker,
							   Composite master) {
		
		final Composite plotArea = new Composite(master, SWT.NONE);
		
		/*************************************************************************
		 * Padding Canvas
		 *************************************************************************/
		
		final Canvas axisCanvas = new Canvas(plotArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, true).
						hint(HPCTraceView.Y_AXIS_WIDTH, 40).applyTo(axisCanvas);
		
		/*************************************************************************
		 * Summary View Canvas
		 *************************************************************************/
		
		summaryCanvas = new SummaryTimeCanvas(parentPart, plotArea, broker);
		summaryCanvas.setLayout(new GridLayout());
		summaryCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		summaryCanvas.setVisible(false);

		/*************************************************************************
		 * Master Composite
		 *************************************************************************/
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(plotArea);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(plotArea);

		/*************************************************************************
		 * Add listeners
		 *************************************************************************/
		
		setToolTipText("The view to show for the whole time range dislayed, the proportion of each subroutine in a certain time.");
	}
	

	@Override
	public void setInput(Object input) {
		summaryCanvas.updateData((SpaceTimeDataController) input);
	}
}
