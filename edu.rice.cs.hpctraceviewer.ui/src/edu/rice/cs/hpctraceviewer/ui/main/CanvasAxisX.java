// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.main;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpcsetting.color.ColorManager;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.internal.TextUtilities;
import edu.rice.cs.hpctraceviewer.ui.operation.AbstractTraceOperation;
import edu.rice.cs.hpctraceviewer.data.TraceDisplayAttribute;


/*******************************************************
 * 
 * Time axis canvas
 * 
 * The canvas draws adaptively a range of axis from time T0 to Tx
 *   according to the current display range.
 *   
 *   If the range changes, it will change the axis automatically.
 *
 *******************************************************/
public class CanvasAxisX extends AbstractAxisCanvas 
	implements PaintListener, IOperationHistoryListener
{		
	private static final int TICK_BIG   = 4;
	private static final int TICK_SMALL = 2;
	
	private final DecimalFormat formatTime;
	
	/**
	 * Ugly variable here to make sure we dispose it at the end
	 */
	private Font fontX;
	
	/***
	 * Constructor of time axis canvas.
	 * 
	 * @param parent
	 * @param style
	 */
	public CanvasAxisX(ITracePart tracePart, Composite parent) {
		super(tracePart, parent);

		formatTime = new DecimalFormat("###,###,###,###,###.##");
		
		tracePart.getOperationHistory().addOperationHistoryListener(this);
	}

	
	@Override
	public void dispose() {
		if (fontX != null && !fontX.isDisposed()) {
			fontX.dispose();
		}
		super.dispose();
	}
	
		
	private void rebuffer() {

		if (getData() == null)
			return;
		
		final SpaceTimeDataController data = (SpaceTimeDataController) getData();
                
        // try to adapt automatically the font height if the height of the canvas isn't sufficient
        // Theoretically, the height is always constant in the beginning of the first appearance. 
        // However, during the view creation, SWT returns the size of height and width are both zero.
        // So Apparently the best way is to compute the height during the paint control :-(

		// ------------------------------------------------------------------------------------------
		// let use GC instead of ImageData since GC allows us to draw lines and
		// rectangles
		// ------------------------------------------------------------------------------------------
		initBuffer();

		final Rectangle area = getClientArea();
		final int viewWidth = area.width;
		final int viewHeight = area.height;

		if (viewWidth == 0 || viewHeight == 0)
			return;

		final Image imageBuffer = new Image(getDisplay(), viewWidth, viewHeight);
		setBuffer(imageBuffer);

		final GC buffer = new GC(imageBuffer);
		
		// the height of the font must be bigger than the height of the canvas minus ticks and empty spaces
		final int space = TICK_BIG + 3;
		int idealFontSize = TextUtilities.getTheRightFontSize(imageBuffer.getDevice(), viewHeight - space);
		if (idealFontSize > 0) {
			var fontData = buffer.getFont().getFontData();
			fontData[0].setHeight(idealFontSize);
			
			if (fontX != null && !fontX.isDisposed())
				fontX.dispose();
			
			fontX = new Font(getDisplay(), fontData);
			buffer.setFont(fontX);
		}
		
		final TraceDisplayAttribute attribute = data.getTraceDisplayAttribute();
		
		TimeUnit dbTimeUnit = data.getTimeUnit();
		
		AxisXTicks xTicks = new AxisXTicks(viewWidth, dbTimeUnit);
		
		// --------------------------------------------------------------------------
		// find the right unit time (s, ms, us, ns) 
		// we want to display ticks to something like:
		//  10s .... 20s ... 30s ... 40s
		// 
		// --------------------------------------------------------------------------
		
		var ticksInfo = xTicks.computeTicks(attribute, str -> {
			if (!buffer.isDisposed())
				return buffer.stringExtent(str).x;
			return 10; // something wrong: the GC has been disposed. Should we throw an exception?
		});
		
		// --------------------------------------------------------------------------
        // Manually fill the client area with the default background color
        // Some platforms don't paint the background properly 
		// --------------------------------------------------------------------------
        Color bgColor = getParent().getBackground();
        Color fgColor = ColorManager.getTextFg(bgColor);
        
		buffer.setBackground(bgColor);
		buffer.setForeground(fgColor);
		buffer.fillRectangle(getClientArea());
		
		// --------------------------------------------------------------------------
		// draw the x-axis
		// --------------------------------------------------------------------------
		
		final int position_y = 0;
		buffer.drawLine(area.x, position_y, area.width, position_y);

		int tick_y_height = position_y + TICK_SMALL;

		var numTicks = ticksInfo.ticks().length;
		
		for(int i=0; i < numTicks; i++) {			
			var axis_x_pos = ticksInfo.ticks()[i];

			// draw the X tick
			buffer.drawLine(axis_x_pos, position_y, axis_x_pos, tick_y_height);
		}
		
		final var userDisplayTimeUnit = ticksInfo.displayTimeUnit();
		final var strTimeUnit = attribute.getTimeUnitName(userDisplayTimeUnit);

		// draw the tick's label 		
		for (int i=0; i<ticksInfo.tick_labels().size(); i++) {
			var tickLabel = ticksInfo.tick_labels().get(i);
			
			var index = tickLabel.tickIndex();
			var axis_x_pos = ticksInfo.ticks()[index];
			var time = tickLabel.tickLabel();

			// convert tick time from database unit time to the current unit time
			
			// in case we can simplify the unit time, convert the time to the display time
			// multiplier is the constant to convert to the display unit time.
			// for instance if the unit time is seconds and display time is minutes,
			// then the multiplier is 1/60
			String strTime = formatTime.format(time) + strTimeUnit;			

			var textSize = buffer.stringExtent(strTime);
			
			// make sure the label is in the middle of the tick, unless for the first tick
			// the first tick is tricky because we usually have no space to draw the text 
			int x_pos = axis_x_pos - (textSize.x / 2);
			
			if (x_pos >= 0 && x_pos+textSize.x < viewWidth) {
				drawTickLabel(buffer, strTime, axis_x_pos, x_pos, position_y);
			} else if (i == 0){
				// exception for the first tick, we can squeeze to the left if we have space
				if (i + 1 < ticksInfo.tick_labels().size()) {
					var nextTick = ticksInfo.tick_labels().get(i+1);
					var nextTickPos = ticksInfo.ticks()[nextTick.tickIndex()];
					var nextLabelPos = nextTickPos - (textSize.x / 2);
					
					if (nextLabelPos > textSize.x + 2) {
						drawTickLabel(buffer, strTime, axis_x_pos, 0, position_y);
					}
				} else {
					// no next tick, it's safe to draw the first label
					drawTickLabel(buffer, strTime, axis_x_pos, 0, position_y);
				}
			}
		}
		var multiplier = ticksInfo.conversionFactor();
		
		attribute.setTimeUnitMultiplier(multiplier);
		attribute.setTimeUnit(ticksInfo.dataTimeUnit());
		attribute.setDisplayTimeUnit(userDisplayTimeUnit);
		
		buffer.dispose();
		
		redraw();
	}
	
	
	private void drawTickLabel(GC gc, String label, int x_tick, int x, int y) {
		gc.drawText(label, x, y + TICK_BIG);
		gc.drawLine(x_tick, y + TICK_SMALL, x_tick, y + TICK_BIG);
	}
	
	@Override
	public void historyNotification(final OperationHistoryEvent event) {

		if (isDisposed())
			return;
		
		if (event.getEventType() == OperationHistoryEvent.DONE) {
			final IUndoableOperation operation = event.getOperation();
			if (!(operation instanceof AbstractTraceOperation)) {
				return;
			}
			final AbstractTraceOperation op = (AbstractTraceOperation) operation;
			if (op.getData() != getData())
				return;
			
			rebuffer();
		}
	}
}
