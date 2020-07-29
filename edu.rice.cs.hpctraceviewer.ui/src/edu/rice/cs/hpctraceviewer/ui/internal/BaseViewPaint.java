package edu.rice.cs.hpctraceviewer.ui.internal;


import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.TimelineDataSet;




/******************************************************
 * 
 * Abstract class to paint depth view and detail view
 * The instance of the children of the class needs to 
 * implement the start and the end method of the painting
 * 
 *
 *******************************************************/
public abstract class BaseViewPaint extends Job
{
	protected boolean changedBounds;
		
	protected SpaceTimeDataController controller;

	final private ISpaceTimeCanvas canvas;
	

	/**
	 * Constructor to paint a view (trace and depth view)
	 * 
	 * @param title name of this view (job title purpose)
	 * @param _data global data of the traces
	 * @param _attributes the attribute of the trace view
	 * @param _changeBound true or false if it requires changes of bound
	 * @param threadExecutor executor
	 */

	public BaseViewPaint(String title, SpaceTimeDataController _data, boolean _changeBound, 
						 ISpaceTimeCanvas canvas) 
	{
		super(title);
		
		changedBounds = _changeBound;
		controller    = _data;
		this.canvas   = canvas;
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus run(IProgressMonitor monitor) {
		IStatus status = Status.OK_STATUS;
		
		//BusyIndicator.showWhile(getDisplay(), getThread());
		if (!paint( monitor))
		{
			status = Status.CANCEL_STATUS;
		}
		
		return status;
	}	

	/**********************************************************************************
	 *	Paints the specified time units and processes at the specified depth
	 *	on the SpaceTimeCanvas using the SpaceTimeSamplePainter given. Also paints
	 *	the sample's max depth before becoming overDepth on samples that have gone over depth.
	 *
	 *	@param canvas   		 The SpaceTimeDetailCanvas that will be painted on.
	 *  @return boolean true of the pain is successful, false otherwise
	 ***********************************************************************************/
	public boolean paint(IProgressMonitor monitor)
	{	
		// depending upon how zoomed out you are, the iteration you will be
		// making will be either the number of pixels or the processors
		int linesToPaint = getNumberOfLines();

		// -------------------------------------------------------------------
		// hack fix: if the number of horizontal pixels is less than 1 we
		// return immediately, otherwise it throws an exception
		// -------------------------------------------------------------------
		if (controller.getPixelHorizontal() <= 0)
			return false;
		
		// -------------------------------------------------------------------
		// initialize the painting (to be implemented by the instance)
		// -------------------------------------------------------------------

		if (!startPainting(linesToPaint, 1, changedBounds))
			return false;

		monitor.beginTask(getName(), linesToPaint);

		// -------------------------------------------------------------------
		// Create multiple threads to collect data
		// -------------------------------------------------------------------
		
		// decompression can be done with multiple threads without accessing gtk (on linux)
		// It looks like there's no major performance effect though

		try {
			launchDataGettingThreads(changedBounds, 1);
			
		} catch (Exception e) {
			e.printStackTrace();
			
			// shutdown the monitor to end the progress bar
			monitor.done();
			return false;
		}
		
		// -------------------------------------------------------------------
		// instantiate queue based on whether we need multi-threading or not.
		// in case of multithreading, we want a thread-safe queue
		// -------------------------------------------------------------------
		final Queue<TimelineDataSet> queue = new ConcurrentLinkedQueue<TimelineDataSet>();

		// -------------------------------------------------------------------
		// case where everything works fine, and all the data has been read,
		//	we paint the canvas using multiple threads
		// -------------------------------------------------------------------
				
		final double xscale = canvas.getScalePixelsPerTime();
		final double yscale = Math.max(canvas.getScalePixelsPerRank(), 1);
		
		
		final BaseTimelineThread thread = getTimelineThread(canvas, xscale, yscale, queue, monitor);
		
		try {
			thread.call();
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		final BasePaintThread paintThread = getPaintThread(queue, linesToPaint,
				controller.getPixelHorizontal(), monitor);

		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				List<ImagePosition> listImages;
				try {
					listImages = paintThread.call();

					for (ImagePosition image : listImages) 
					{
						if (!monitor.isCanceled())
							drawPainting(canvas, image);
						else
							break;
					}				

					// notify user that we have finished painting
					endPainting(monitor.isCanceled());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		// -------------------------------------------------------------------
		// draw to the canvas
		// -------------------------------------------------------------------
		
		monitor.done();
		changedBounds = false;

		return !monitor.isCanceled();
	}
	
	
	//------------------------------------------------------------------------------------------------
	// abstract methods 
	//------------------------------------------------------------------------------------------------
	
	/**
	 * Initialize the paint, before creating the threads to paint.
	 * The method return false to exit the paint, true to paint.
	 * 
	 * The implementer is also responsible to reset its counter (yikes!)
	 * 
	 * @param linesToPaint
	 * @param changedBounds
	 * @return false will exit the painting
	 */
	abstract protected boolean startPainting(int linesToPaint, int numThreads, boolean changedBounds);
	
	/*****
	 * notification for the termination of painting
	 * 
	 * @param isCanceled : flag if the process has been canceled or not
	 */
	abstract protected void endPainting(boolean isCanceled);
	
	/***
	 * start painting an image to the canvas
	 * 
	 * @param canvas: canvas to be painted
	 * @param imagePosition : a pair of image and position
	 */
	abstract protected void drawPainting(ISpaceTimeCanvas canvas, ImagePosition imagePosition);
	
	/**
	 * Retrieve the number of lines to paint 
	 * @return
	 */
	abstract protected int getNumberOfLines();
	
	/****
	 * launching threads for remote communication
	 * 
	 * @param changedBounds
	 * @param numThreads
	 * @throws IOException
	 */
	abstract protected void launchDataGettingThreads(boolean changedBounds, int numThreads) 
			throws IOException;

	/****
	 * get a thread for collecting timeline data
	 * @param canvas
	 * @param xscale
	 * @param yscale
	 * @param queue
	 * @param timelineDone
	 * @return
	 */
	abstract protected BaseTimelineThread  getTimelineThread(ISpaceTimeCanvas canvas, double xscale, double yscale,
			Queue<TimelineDataSet> queue, IProgressMonitor monitor);
	
	/***
	 * get a thread for painting a number of lines
	 * @param queue
	 * @param numLines
	 * @param timelineDone
	 * @param device
	 * @param width
	 * @return
	 */
	abstract protected BasePaintThread getPaintThread( Queue<TimelineDataSet> queue, int numLines, 
													   int width, IProgressMonitor monitor);
}
