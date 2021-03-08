package edu.rice.cs.hpctraceviewer.ui.depthEditor;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.ui.internal.TraceEventData;
import edu.rice.cs.hpctraceviewer.ui.util.IConstants;
import edu.rice.cs.hpctraceviewer.ui.util.Utility;

public class DepthEditor implements EventHandler 
{
	private final static String ICON_MAX_DEPTH = "IconMaxDepth";
	private final static String MAX_DEPTH_FILE =  "platform:/plugin/edu.rice.cs.hpctraceviewer.ui/resources/max-depth16.png";

	private final Spinner depthEditor;	
	private final Button maxDepthButton;	
	private final IEventBroker eventBroker;	
	
	private boolean enableAction = false;
	private SpaceTimeDataController stData;

	public DepthEditor(Composite parent, IEventBroker eventBroker) {
		this.eventBroker = eventBroker;
		
		Composite depthArea = new Composite(parent, SWT.BORDER); 
		
		final Label lblDepth = new Label(depthArea, SWT.LEFT);
		lblDepth.setText("Depth: ");
		
		depthEditor = new Spinner(depthArea, SWT.BORDER);
		depthEditor.setMinimum(0);
		depthEditor.setPageIncrement(1);
		
		depthEditor.setLayout(new GridLayout());
		GridData depthData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		depthData.widthHint = 50;
		depthEditor.setLayoutData(depthData);
		depthEditor.setVisible(false);
		
		maxDepthButton = new Button(depthArea, 0);		
		maxDepthButton.setEnabled(false);
		
		Image image = Utility.getImage(MAX_DEPTH_FILE, ICON_MAX_DEPTH);
		maxDepthButton.setImage(image);

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
			public void widgetDefaultSelected(SelectionEvent e) {}
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
				broadcast(value);
			}
		});
		eventBroker.subscribe(IConstants.TOPIC_DEPTH_UPDATE, this);
	}

	public void setInput(SpaceTimeDataController data) {
		this.stData = data;
		
		// guard : no action has to be taken at the moment;
		//setEnableAction(false);
		
		// Fix bug #14: extra depth https://github.com/HPCToolkit/hpcviewer.e4/issues/14
		// By default hpcdata computes the depth based on the line scope, not procedure scope.
		// TODO: Ideally we need correct the max depth inside hpcdata, but unfortunately other classes
		//  like many in depth views, depend on the definition that max_depth = max_call_path + 1
		// Hence, it's simpler to correct the max depth here.
		
		final int maxDepth = data.getMaxDepth()-1;
		depthEditor.setSelection(0);
		depthEditor.setMaximum(maxDepth);		
		depthEditor.setVisible(true);
		depthEditor.setToolTipText("Change the current depth.\nMax depth is " + maxDepth);
		
		setSelection(data.getDefaultDepth());

		maxDepthButton.setToolTipText("Set to max depth: " + maxDepth);
		maxDepthButton.setData(Integer.valueOf(maxDepth));
		maxDepthButton.setEnabled(true);
	}

	private void setSelection(int depth) {
		
		setEnableAction(false);
		depthEditor.setSelection(depth);
		setEnableAction(true);

	}
	
	public void setEnableAction(boolean enabled) {
		enableAction = enabled;
	}
	
	public boolean getEnableAction() {
		return enableAction;
	}

	public void dispose() {
		eventBroker.unsubscribe(this);
	}
	
	public void broadcast(int depth) {
		if (getEnableAction()) {
			// broadcast the change to other components
			TraceEventData data = new TraceEventData(stData, this, depth);
			eventBroker.post(IConstants.TOPIC_DEPTH_UPDATE, data);
		}
	}

	@Override
	public void handleEvent(Event event) {

		Object obj = event.getProperty(IEventBroker.DATA);
		if (obj == null) return;
		
		TraceEventData eventData = (TraceEventData) obj;
		if (eventData.source == this || eventData.data != this.stData)
			return;

		final int depth = (Integer)eventData.value;

		setSelection(depth);
	}
}
