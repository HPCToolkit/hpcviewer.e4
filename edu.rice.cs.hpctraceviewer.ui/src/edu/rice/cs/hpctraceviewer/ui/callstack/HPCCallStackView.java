package edu.rice.cs.hpctraceviewer.ui.callstack;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimelineService;
import edu.rice.cs.hpctraceviewer.data.util.Constants;
import edu.rice.cs.hpctraceviewer.ui.base.AbstractBaseItem;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.depthEditor.DepthEditor;


/**A view for displaying the call path viewer and minimap.*/
//all the GUI setup for the call path and minimap are here//
public class HPCCallStackView extends AbstractBaseItem
{
	private CallStackViewer csViewer;
	private DepthEditor     depthEditor;
	
	private SpaceTimeDataController data;
	/***
	 * Constructor to initialize the tab item
	 * @param parent parent which is a CTabFolder
	 * @param style
	 */
	public HPCCallStackView(CTabFolder parent, int style) {
		super(parent, style);
	}

	@Override
	public void createContent(ITracePart parentPart, 
							  IEclipseContext context, 
							  IEventBroker broker,
							  Composite master) {
		
		ProcessTimelineService ptlService = (ProcessTimelineService) context.get(Constants.CONTEXT_TIMELINE);

		/*************************************************************************
		 * Master Composite
		 ************************************************************************/
		
		master.setLayout(new GridLayout());
		master.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
		
		/*************************************************************************
		 * Depth area. Consist of:
		 * - Depth View Spinner (the thing with the text box and little arrow buttons)
		 * - max depth (a shortcut to go to the maximum depth). See issue #64
		 ************************************************************************/
		
		
		/*************************************************************************
		 * CallStackViewer
		 ************************************************************************/
		csViewer = new CallStackViewer(parentPart, master, this, ptlService, broker);
		
		depthEditor = new DepthEditor(master, broker);
		depthEditor.setEnableAction(false);

		setToolTipText("The view to show the depth and the actual call path for the point selected by the Trace View's crosshair");
	}
		
		

	@Override
	public void setInput(Object input) {
		data = (SpaceTimeDataController)input;
		
		csViewer.setInput(data);
		depthEditor.reset(data);
		
		// instead of updating the content of the view, we just make the table
		// visible, and let other event to trigger the update content.
		// at this point, a data may not be ready to be processed
		csViewer.getTable().setVisible(true);

		// enable action
		depthEditor.setEnableAction(true);
	}
}
