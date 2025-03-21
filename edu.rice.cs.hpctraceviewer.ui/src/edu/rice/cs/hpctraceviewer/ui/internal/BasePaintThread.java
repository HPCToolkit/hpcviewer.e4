// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import edu.rice.cs.hpctraceviewer.data.BaseDataVisualization;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.TimelineDataSet;



/*****************************************************************
 *
 * Abstract base class thread to paint a canvas that can be either 
 * 	detail canvas or depth canvas.
 * 
 * The class has a future variable List<ImagePosition> which is
 *
 *****************************************************************/
public abstract class BasePaintThread implements Callable<List<ImagePosition>> 
{
	protected final int width;
	private final IProgressMonitor monitor;

	private final Queue<TimelineDataSet> list;
	private final List<ImagePosition> listOfImages;
	private final int numberOfTotalLines;
	
	protected final SpaceTimeDataController stData;
	private final AtomicInteger currentPaint;
	
	/****
	 * constructor of the class, requiring a queue of list of data (per line) to be
	 * visualized on a set of images. The queue can be thread-safe (in case of multithreaded)
	 * or unsafe (case of single threaded). 
	 * <p>
	 * To retrieve the list of images, the caller needs to call the method get() from
	 * {@link java.util.concurrent.Callable} 
	 * 
	 * @param stData SpaceTimeDataController
	 * @param list : the queue of TimelineDataSet data. Use a thread-safe queue for multi-threads
	 * @param numberOfTotalLines : number of total images or lines
	 * @param currentPaint AtomicInteger Counter of number of consumed data for painting
	 * @param width : the width of the view
	 * @param monitor progress monitor
	 */
	protected BasePaintThread(
			SpaceTimeDataController stData, 
			Queue<TimelineDataSet> list, 
			int numberOfTotalLines, 
			AtomicInteger currentPaint,
			int width, 
			IProgressMonitor monitor) {
		
		Assert.isNotNull(list);
		
		this.stData 			= stData;		
		this.width   			= width;
		this.monitor 			= monitor;
		this.list 				= list;
		this.numberOfTotalLines = numberOfTotalLines;
		this.currentPaint		= currentPaint;
		listOfImages = new ArrayList<>(list.size());
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	public List<ImagePosition> call() throws Exception {

		while( /*! list.isEmpty() 				 		 // while there are tasks to do 
				|| 						*/				 // or	
				currentPaint.get() < numberOfTotalLines  // the current painting is not done yet
			 )
		{
			if (monitor.isCanceled()) {
				dispose();
				return listOfImages;
			}
			// ------------------------------------------------------------------
			// get the task to do from the list and compute the height and the position
			// if the list is empty, it means the data collection threads haven't finished 
			//	their work yet. It's better to wait and sleep a bit 
			// ------------------------------------------------------------------

			final TimelineDataSet setDataToPaint = list.poll();
			if (setDataToPaint == null) {
				Thread.sleep(40);
				continue;
			}
			if (setDataToPaint == TimelineDataSet.NULLTimeline) {
				// empty trace
				currentPaint.incrementAndGet();
				continue;
			}
			final int height = setDataToPaint.getHeight();
			final int position = setDataToPaint.getLineNumber();
			
			// ------------------------------------------------------------------
			// initialize the painting, the derived class has to create image ready
			// ------------------------------------------------------------------
			initPaint(/*device,*/ width, height);

			// ------------------------------------------------------------------
			// a line can contains many trace data (one trace data equals one rectangle)
			// we just assume here that each trace data is different and each 
			//	has different color
			// ------------------------------------------------------------------
			for(BaseDataVisualization data : setDataToPaint.getList()) 
			{
				if (monitor.isCanceled()) {
					dispose();
					return listOfImages;
				}
				
				// ------------------------------------------------------------------
				// paint the image
				// ------------------------------------------------------------------
				paint(position, data, height);
			}
			// ------------------------------------------------------------------
			// finalize phase
			// ------------------------------------------------------------------
			final ImagePosition imgPos = finalizePaint(position);
			
			listOfImages.add(imgPos);
			currentPaint.incrementAndGet();

		}

		return listOfImages;
	}
	
	/*****
	 * Abstract method to initialize the paint. 
	 * The derived class can use this method to create images and GC before painting it
	 * 
	 * @param device : device to create the image
	 * @param width : the width of the image
	 * @param height : the height of the image
	 */
	protected abstract void initPaint(/*Device device, */int width, int height);
	
	/*****
	 * the actual method to paint a trace image
	 * 
	 * @param position : the rank or the position line number of the image
	 * @param data : the data to be painted
	 * @param height : the height of the image
	 */
	protected abstract void paint(int position, BaseDataVisualization data, int height);
	
	/********
	 * Finalizing the image. 
	 * 
	 * @param linenum : the position of the line number of the image
	 * @return
	 */
	protected abstract ImagePosition finalizePaint(int linenum);
	
	
	/****
	 * Free allocated resources. To be called by the end of the sessionS
	 */
	public abstract void dispose();
	
	/***
	 * basic method to paint on a gc
	 * 
	 * @param gc
	 * @param p_start
	 * @param p_end
	 * @param height
	 * @param color
	 */
	protected void paint(GC gc, int p_start, int p_end, int height, Color color) {
		
		int width = p_end - p_start;
		if (width <= 0)
			return;
		
		gc.setBackground(color);
		gc.fillRectangle(p_start, 0, width, height);
	}
}
