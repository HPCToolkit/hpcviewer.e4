package edu.rice.cs.hpctraceviewer.ui.main;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpcsetting.color.ColorManager;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
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
	private static final int TICK_X_PIXEL = 110;
	private static final int MINIMUM_PIXEL_BETWEEN_TICKS = 10;
	private static final int TICK_BIG   = 4;
	private static final int TICK_SMALL = 2;
	
	private final DecimalFormat formatTime;
	
	private Font fontX;
	
	/***
	 * Constructor of time axis canvas.
	 * 
	 * @param parent
	 * @param style
	 */
	public CanvasAxisX(ITracePart tracePart, Composite parent, int style) {
		super(tracePart, parent, SWT.NO_BACKGROUND | style);

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

		final int viewWidth = getBounds().width;
		final int viewHeight = getBounds().height;

		if (viewWidth == 0 || viewHeight == 0)
			return;

		final Image imageBuffer = new Image(getDisplay(), viewWidth, viewHeight);
		setBuffer(imageBuffer);

		final GC buffer = new GC(imageBuffer);

		FontData []fd = buffer.getFont().getFontData();
		
		final String text = "1,";
		int height = buffer.stringExtent(text).y;
		
		// the height of the font must be bigger than the height of the canvas minus ticks and empty spaces
		final int space = TICK_BIG + 3;
		
		while (viewHeight > 0 && height > viewHeight-space) {
			// the font is too big

			int fontHeight = fd[0].getHeight() - 1;
			if (fontHeight < 0)
				return;
			
			fd[0].setHeight(fontHeight);
			if (fontX != null && !fontX.isDisposed()) {
				fontX.dispose();
			}
			fontX = new Font(getDisplay(), fd);
			buffer.setFont(fontX);
			
			height = buffer.stringExtent(text).y;
		}

		final TraceDisplayAttribute attribute = data.getTraceDisplayAttribute();
		final Rectangle area = getClientArea();
		
		TimeUnit dbTimeUnit = data.getTimeUnit();
		
		// --------------------------------------------------------------------------
		// find the right unit time (s, ms, us, ns) 
		// we want to display ticks to something like:
		//  10s .... 20s ... 30s ... 40s
		// 
		// --------------------------------------------------------------------------
		
		TimeUnit displayTimeUnit = attribute.computeDisplayTimeUnit(data);
		
		// --------------------------------------------------------------------------
		// finding some HINTs of number of ticks, and distance between ticks 	
		// --------------------------------------------------------------------------
		
		double numTicks  = (double)area.width / TICK_X_PIXEL;
		double fraction  = attribute.getTimeInterval() / numTicks;

		// --------------------------------------------------------------------------
		// find the nice rounded number
		// if dt < 10:  1, 2, 3, 4...
		// if dt < 100: 10, 20, 30, 40, ..
		// ...
		// --------------------------------------------------------------------------
		
		long t1 = attribute.getTimeBegin();
		long t2 = (long) (t1 + fraction);
		long dt = displayTimeUnit.convert(t2 - t1, dbTimeUnit);
		
		// there is nothing we can do if the difference between the ticks is less than 1 ns.
		if (dt<1) {
			int ordinal = attribute.getTimeUnitOrdinal(displayTimeUnit);
			if (ordinal>0 && (t1 != t2)) {
				ordinal--;
				displayTimeUnit = attribute.getTimeUnit(ordinal);
				
				// recompute dt with the new time unit
				dt = displayTimeUnit.convert(t2-t1, dbTimeUnit);
			} else {
				dt = 1;
			}
		}

		// the time unit displayed for users
		// sometime the time unit is millisecond, but user can see it as second like:
		// 2.1s, 2.2s instead of 2100ms and 2200ms
		TimeUnit userDisplayTimeUnit = displayTimeUnit;

		// find rounded delta_time to log 10:
		// if delta_time is 12 --> rounded to 10
		// 					3  --> rounded to 1
		// 					32 --> rounded to 10
		// 					312 --> rounded to 100
		
		int logdt 	 = (int) Math.log10(dt);
		long dtRound = (int) Math.pow(10, logdt);
		
		int maxTicks = area.width/MINIMUM_PIXEL_BETWEEN_TICKS; 
		int numAxisLabel;
		do {
			numAxisLabel = (int) (displayTimeUnit.convert(attribute.getTimeInterval(), dbTimeUnit) / dtRound);
			if (numAxisLabel > maxTicks) {
				dtRound *= 10;
			}
		} while (numAxisLabel > maxTicks);
		
		float multiplier = 1; 
		
		// Issue #20 : avoid displaying big numbers.
		// if the time is 1200ms we should display it as 1.2s
		// this doesn't change the real unit time. It's just for the display
		String userUnitTime = attribute.getTimeUnitName(displayTimeUnit);

		if (dtRound >= 100) {
			final TimeUnit tu = attribute.increment(displayTimeUnit);
			userUnitTime      = attribute.getTimeUnitName(tu);
			
			// temporary fix for issue #304
			// need to use Java time unit conversion from to coarser grain time unit.
			// who know we need to convert to minutes or hours 
			var conversion    =  userDisplayTimeUnit.convert(1, tu);
			multiplier = (float) 1/conversion;
			
			// if the delta time is not multiple of 1000 (like from micro to mili),
			// we need to adjust the distance between ticks so that the label is 
			// nicely even (not a multiple of .67, ...)
			if (1000 % conversion != 0) {
				dtRound = conversion;
				numAxisLabel = (int) (displayTimeUnit.convert(attribute.getTimeInterval(), dbTimeUnit) / dtRound);
			}
			userDisplayTimeUnit = tu;
		}

		double timeBegin    = displayTimeUnit.convert(attribute.getTimeBegin(), dbTimeUnit);
		
		// round the time to the upper bound
		// if the time is 22, we round it to 30
		
		long remainder = (long) timeBegin % dtRound;
		if (remainder > 0)
			timeBegin = timeBegin + (dtRound - remainder);
		
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
		
		// it is possible in some cases that the height is so small that we cannot
		// display axis
		
		final int position_y = 0;
		buffer.drawLine(area.x, position_y, area.width, position_y);
		
		Point prevTextArea  = new Point(0, 0);
		int   prevPositionX = 0;
		long  displayTimeBegin = displayTimeUnit.convert(attribute.getTimeBegin(), dbTimeUnit);
		double deltaXPixels    = (double)attribute.getPixelHorizontal() / 
								 displayTimeUnit.convert(attribute.getTimeInterval(), dbTimeUnit);

		// --------------------------------------------------------------------------
		// draw the ticks and the labels if there's enough space
		// --------------------------------------------------------------------------
		final String maxText    = formatTime.format(timeBegin) + "XX";
		final Point maxTextArea = buffer.stringExtent(maxText);
		final int deltaTick     = convertTimeToPixel(displayTimeBegin, (long)timeBegin + dtRound, deltaXPixels);
		final boolean isFitEnough = deltaTick > maxTextArea.x;

		for(int i=0; i <= numAxisLabel; i++) {			
			double time      = timeBegin + dtRound * i;			
			int axis_x_pos	 = (int) convertTimeToPixel(displayTimeBegin, (long)time, deltaXPixels);
			int axis_tick_mark_height = position_y+TICK_SMALL;

			// we want to draw the label if the number is nicely readable
			// nice numbers: 1, 2, 4, ...
			// not nice numbers: 1.1, 2.3, ...
			double timeToAppear = time * multiplier;
			boolean toDrawLabel = isFitEnough        ||
								  (time % 200 == 0)  ||
								  (timeToAppear % 2 == 0) ;
			
			if (i==0 || toDrawLabel) {
				String strTime   = formatTime.format(timeToAppear) + userUnitTime;			
				Point textArea   = buffer.stringExtent(strTime);

				// by default x position is in the middle of the tick
				int position_x   = axis_x_pos - (textArea.x/2);
				
				// make sure we don't trim the text in the beginning of the axis
				if (position_x<0) {
					position_x = 0;
				}
				// make sure x position is not beyond the view's width
				else if (position_x + textArea.x > area.width) {
					position_x = axis_x_pos - textArea.x;
				}
				// draw the label only if we have space
				if (i==0 || prevPositionX+prevTextArea.x + 10 < position_x) {
					buffer.drawText(strTime, position_x, position_y + TICK_BIG+1);

					prevTextArea.x = textArea.x;
					prevPositionX  = position_x;
					
					axis_tick_mark_height+=TICK_BIG;
				}
			}
			// always draw the ticks
			buffer.drawLine(axis_x_pos, position_y, axis_x_pos, axis_tick_mark_height);
		}
		attribute.setTimeUnitMultiplier(multiplier);
		attribute.setTimeUnit(displayTimeUnit);
		attribute.setDisplayTimeUnit(userDisplayTimeUnit);
		
		buffer.dispose();
		
		redraw();
	}
	
	/*****
	 * convert from time to pixel
	 * 
	 * @param displayTimeBegin current attribute time configuration
	 * @param time the time to convert
	 * @param deltaXPixels the distance for two different time (in pixel)
	 * 
	 * @return pixel (x-axis)
	 */
	private int convertTimeToPixel(long displayTimeBegin,
								   long time, 
								   double deltaXPixels)
	{
		// define pixel : (time - TimeBegin) x number_of_pixel_per_time 
		//				  (time - TimeBegin) x (numPixelsH/timeInterval)
		long dTime = time-displayTimeBegin;
		return(int) (deltaXPixels * dTime);
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
