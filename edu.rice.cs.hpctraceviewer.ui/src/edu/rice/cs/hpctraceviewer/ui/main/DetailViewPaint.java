package edu.rice.cs.hpctraceviewer.ui.main;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;


import edu.rice.cs.hpctraceviewer.data.ImageTraceAttributes;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.TimelineDataSet;
import edu.rice.cs.hpctraceviewer.ui.painter.BasePaintThread;
import edu.rice.cs.hpctraceviewer.ui.painter.BaseViewPaint;
import edu.rice.cs.hpctraceviewer.ui.painter.ISpaceTimeCanvas;
import edu.rice.cs.hpctraceviewer.ui.painter.ImagePosition;
import edu.rice.cs.hpctraceviewer.ui.timeline.BaseTimelineThread;

/******************************************************
 * 
 * Painting class for detail view (space-time view)
 *
 ******************************************************/
public class DetailViewPaint extends BaseViewPaint {
		
	/** maximum number of records to display **/
	static public final int MAX_RECORDS_DISPLAY = 99;
	/** text when we reach the maximum of records to display **/
	static public final String TOO_MANY_RECORDS = ">" + String.valueOf(MAX_RECORDS_DISPLAY) ;
	
	final private Point maxTextSize;

	private final GC masterGC;
	private final GC origGC;
	private final Device device;
	
	final private AtomicInteger currentLine, numDataCollected;
	final private int numLines;

	private boolean debug;
	
	public DetailViewPaint(final Device device, final GC masterGC, final GC origGC, SpaceTimeDataController data,
			ImageTraceAttributes attributes, int numLines, boolean changeBound,
			ISpaceTimeCanvas canvas, ExecutorService threadExecutor) 
	{
		super("Main trace view", data, attributes, changeBound, canvas, threadExecutor);
		this.masterGC = masterGC;
		this.origGC   = origGC;
		this.numLines = numLines;
		this.device   = device;
		
		// check if we need to print the text information on the canvas
		
		// initialize the size of maximum text
		//	the longest text should be: ">99(>99)"
		maxTextSize = masterGC.textExtent(TOO_MANY_RECORDS + "(" + TOO_MANY_RECORDS + ")");
		
		currentLine = new AtomicInteger(0);
		numDataCollected = new AtomicInteger(0);
	}

	@Override
	protected boolean startPainting(int linesToPaint, int numThreads, boolean changedBounds) {
		return true;
	}

	@Override
	protected int getNumberOfLines() {
		return numLines;
	}

	@Override
	protected BaseTimelineThread getTimelineThread(ISpaceTimeCanvas canvas, double xscale,
			double yscale, Queue<TimelineDataSet> queue, IProgressMonitor monitor) {

		return new TimelineThread(controller, attributes, null, changedBounds,   
				yscale, queue, numDataCollected, numLines, monitor);
	}

	@Override
	protected void launchDataGettingThreads(boolean changedBounds,
			int numThreads) throws IOException {
		controller.fillTracesWithData( changedBounds, numThreads);
	}

	@Override
	protected BasePaintThread getPaintThread(
			Queue<TimelineDataSet> queue, int numLines, 
			int width, IProgressMonitor monitor) {

		return new DetailPaintThread(device, controller, queue, numLines, 
				numDataCollected, currentLine, 
				width, maxTextSize, debug, monitor);
	}

	@Override
	protected void drawPainting(ISpaceTimeCanvas canvas,
			ImagePosition imagePosition) {

		DetailImagePosition imgDetailLine = (DetailImagePosition)imagePosition;
		double yscale = Math.max(canvas.getScalePixelsPerRank(), 1);

		int yposition = (int) Math.round(imgDetailLine.position * yscale);
		
		System.out.println("dvp master-draw " + imagePosition.position);

		// put the image onto the canvas
		masterGC.drawImage(imgDetailLine.image, 0, yposition);
		
		System.out.println("dvp orig draw " + imagePosition.position);
		
		origGC.drawImage(imgDetailLine.imageOriginal, 0, imgDetailLine.position);
				
		System.out.print("dvp dispose " + imagePosition.position + ": ");
		imgDetailLine.image.dispose();
		imgDetailLine.imageOriginal.dispose();
		
		System.out.println("ok");
	}

	@Override
	protected void endPainting(boolean isCanceled) {
		// TODO Auto-generated method stub
		
	}
}
