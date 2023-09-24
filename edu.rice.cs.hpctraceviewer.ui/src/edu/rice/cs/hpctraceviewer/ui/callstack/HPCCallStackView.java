package edu.rice.cs.hpctraceviewer.ui.callstack;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.ui.base.AbstractBaseItem;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;


/**A view for displaying the call path viewer and minimap.*/
//all the GUI setup for the call path and minimap are here//
public class HPCCallStackView extends AbstractBaseItem
{
	private CallStackViewer csViewer;
	
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
		
		/*************************************************************************
		 * Master Composite
		 ************************************************************************/
		
		master.setLayout(new GridLayout());
		master.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
		
		/*************************************************************************
		 * CallStackViewer
		 ************************************************************************/
		csViewer = new CallStackViewer(parentPart, master, broker);

		setToolTipText("The view to show the call-stack depth and the actual call path for the point selected by the Main View's crosshair");
	}
		
		

	@Override
	public void setInput(Object input) {
		data = (SpaceTimeDataController)input;
		
		csViewer.setInput(data);
		
		// instead of updating the content of the view, we just make the table
		// visible, and let other event to trigger the update content.
		// at this point, a data may not be ready to be processed
		csViewer.getTable().setVisible(true);
	}
}
