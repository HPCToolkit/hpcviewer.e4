package edu.rice.cs.hpctraceviewer.ui.minimap;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.ui.base.AbstractBaseItem;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;

public class MiniMap extends AbstractBaseItem 
{
	private SpaceTimeMiniCanvas miniCanvas;

	public MiniMap(CTabFolder parent, int style) {
		super(parent, style);
	}

	@Override
	public void createContent(ITracePart parentPart, IEclipseContext context, IEventBroker broker,
			Composite master) {
		
		final Composite miniArea = new Composite(master, SWT.BORDER_DASH);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(miniArea);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(miniArea);

		Label lbl = new Label(miniArea, SWT.BORDER);
		lbl.setText("Mini map");
		
		miniCanvas = new SpaceTimeMiniCanvas(miniArea);
		miniCanvas.setLayout(new GridLayout());
		GridData miniCanvasData = new GridData(SWT.CENTER, SWT.BOTTOM, true, false);
		miniCanvasData.heightHint = 100;
		miniCanvasData.widthHint = 140;
		miniCanvas.setLayoutData(miniCanvasData);
		
		miniCanvas.setToolTipText("The view to show the portion of the execution shown by the Trace View," +
								  "relative to process/time dimensions");
	}

	@Override
	public void setInput(Object input) {
		miniCanvas.updateView((SpaceTimeDataController) input);
	}
}
