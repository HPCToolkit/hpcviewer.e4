package edu.rice.cs.hpctraceviewer.ui.callstack;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimelineService;
import edu.rice.cs.hpctraceviewer.data.util.Constants;
import edu.rice.cs.hpctraceviewer.ui.AbstractBaseItem;
import edu.rice.cs.hpctraceviewer.ui.ITracePart;


/**A view for displaying the call path viewer and minimap.*/
//all the GUI setup for the call path and minimap are here//
public class HPCCallStackView extends AbstractBaseItem
{
	private CallStackViewer csViewer;
	
	Spinner depthEditor;
	
	private Button maxDepthButton;
	
	private boolean enableAction = false;

	
	public HPCCallStackView(CTabFolder parent, int style) {
		super(parent, style);
	}

	@Override
	public void createContent(ITracePart parentPart, 
							  IEclipseContext context, 
							  IEventBroker broker,
							  Composite master) {
		setEnableAction(false);
		ProcessTimelineService ptlService = (ProcessTimelineService) context.get(Constants.CONTEXT_TIMELINE);
		setupEverything(master, ptlService);
	}
	
	private void setupEverything(Composite master, ProcessTimelineService ptlService)
	{
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
		
		Composite depthArea = new Composite(master, SWT.BORDER); 
		
		final Label lblDepth = new Label(depthArea, SWT.LEFT);
		lblDepth.setText("Depth: ");
		
		depthEditor = new Spinner(depthArea, SWT.EMBEDDED);
		depthEditor.setMinimum(0);
		depthEditor.setPageIncrement(1);
		
		depthEditor.setLayout(new GridLayout());
		GridData depthData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		depthData.widthHint = 50;
		depthEditor.setLayoutData(depthData);
		depthEditor.setVisible(false);
		
		maxDepthButton = new Button(depthArea, 0);
		maxDepthButton.setText("Max depth");
		maxDepthButton.setEnabled(false);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(maxDepthButton);
		
		GridDataFactory.fillDefaults().grab(true, false).applyTo(depthArea);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(depthArea);
		
		maxDepthButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!getEnableAction())
					return;
				
				Integer depth = (Integer) maxDepthButton.getData();
				if (depth == null || depth.intValue() <= 0)
					return;

				depthEditor.setSelection(depth);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		depthEditor.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if (!getEnableAction())
					return;
				
				String string = depthEditor.getText();
				int value = 0;
				if (string.length()<1) {
					// be careful: on linux/GTK, any change in the spinner will consists of two steps:
					//  1) empty the string
					//  2) set with the specified value
					// therefore, we consider any empty string to be illegal
					return;
				} else {
					try {
						value = Integer.valueOf(string);
					} catch (final NumberFormatException errorException) {
						e.display.asyncExec(new Runnable() {
							
							@Override
							public void run() {
								Color yellow = depthEditor.getDisplay().getSystemColor(SWT.COLOR_YELLOW);
								depthEditor.setBackground(yellow);
								depthEditor.setToolTipText("Incorrect number: " + errorException.getMessage());
							}
						});
						return;
					}
				}
				
				final int maximum = depthEditor.getMaximum();
				int minimum = 0;

				if (value > maximum) {
					value = maximum;
					
					e.display.asyncExec(new Runnable() {
						
						@Override
						public void run() {
							Shell shell = e.widget.getDisplay().getActiveShell();
							MessageDialog.openWarning(shell, "Value not allowed", 
									  "The value is higher than the maximum depth (" + maximum +").");
						}
					});
				}
				if (value < minimum) {
					value = minimum;
				}
				csViewer.setDepth(value);
			}
		});
		
		/*************************************************************************
		 * CallStackViewer
		 ************************************************************************/
		csViewer = new CallStackViewer(master, this, ptlService);
		
		setToolTipText("The view to show the depth and the actual call path for the point selected by the Trace View's crosshair");
		
		/*************************************************************************
		 * MiniMap
		 ************************************************************************/
		/*
		Label l = new Label(master, SWT.SINGLE);
		l.setText("Mini Map");
		miniCanvas = new SpaceTimeMiniCanvas(master);
		miniCanvas.setLayout(new GridLayout());
		GridData miniCanvasData = new GridData(SWT.CENTER, SWT.BOTTOM, true, false);
		miniCanvasData.heightHint = 100;
		miniCanvasData.widthHint = 140;
		miniCanvas.setLayoutData(miniCanvasData);
		
		miniCanvas.setVisible(false);
		
		miniCanvas.setToolTipText("The view to show the portion of the execution shown by the Trace View," +
								  "relative to process/time dimensions");
		*/
	}
		
	
	public void updateView(SpaceTimeDataController _stData) 
	{
		// guard : no action has to be taken at the moment;
		setEnableAction(false);
				
		final int maxDepth = _stData.getMaxDepth();
		depthEditor.setSelection(0);
		depthEditor.setMaximum(maxDepth);		
		depthEditor.setVisible(true);
		depthEditor.setToolTipText("Change the current depth.\nMax depth is " + maxDepth);
		
		int depth = _stData.getAttributes().getFrame().depth;
		depthEditor.setSelection(depth);

		maxDepthButton.setToolTipText("Set to max depth: " + maxDepth);
		maxDepthButton.setData(Integer.valueOf(maxDepth));
		maxDepthButton.setEnabled(true);

		// instead of updating the content of the view, we just make the table
		// visible, and let other event to trigger the update content.
		// at this point, a data may not be ready to be processed
		csViewer.getTable().setVisible(true);
		/*
		this.miniCanvas.updateView(_stData);
		
		miniCanvas.setVisible(true);
		*/
		// enable action
		setEnableAction(true);
	}

	private void setEnableAction(boolean enabled) {
		enableAction = enabled;
	}
	
	private boolean getEnableAction() {
		return enableAction;
	}
	

	@Override
	public void setInput(Object input) {
		csViewer.setInput((SpaceTimeDataController)input);
		updateView((SpaceTimeDataController) input);
	}
}
