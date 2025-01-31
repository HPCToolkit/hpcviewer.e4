// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.main;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.TimelineDataSet;
import edu.rice.cs.hpctraceviewer.ui.base.ISpaceTimeCanvas;
import edu.rice.cs.hpctraceviewer.ui.internal.BasePaintThread;
import edu.rice.cs.hpctraceviewer.ui.internal.BaseTimelineThread;
import edu.rice.cs.hpctraceviewer.ui.internal.BaseViewPaint;
import edu.rice.cs.hpctraceviewer.ui.internal.ImagePosition;

/******************************************************
 * 
 * Painting class for detail view (space-time view)
 *
 ******************************************************/
public class DetailViewPaint extends BaseViewPaint 
{		
	/** maximum number of records to display **/
	public static final int MAX_RECORDS_DISPLAY = 99;
	/** text when we reach the maximum of records to display **/
	public static final String TOO_MANY_RECORDS = MAX_RECORDS_DISPLAY + "+";
	
	private final Point maxTextSize;

	private final GC masterGC;
	private final GC origGC;
	private final Device device;
	
	private final AtomicInteger currentLine;
	private final int numLines;

	private boolean debug;

	
	/***
	 * Create a job to paint the main canvas view
	 * @param device
	 * @param masterGC
	 * @param origGC
	 * @param data
	 * @param numLines
	 * @param changeBound
	 * @param canvas
	 */
	public DetailViewPaint(final Device device, final GC masterGC, final GC origGC, SpaceTimeDataController data,
			int numLines, boolean changeBound,
			ISpaceTimeCanvas canvas) 
	{
		super("Main trace view", data, changeBound, canvas);
		this.masterGC = masterGC;
		this.origGC   = origGC;
		this.numLines = numLines;
		this.device   = device;
		
		// check if we need to print the text information on the canvas
		
		// initialize the size of maximum text
		//	the longest text should be: ">99(>99)"
		maxTextSize = masterGC.textExtent(TOO_MANY_RECORDS + "(" + TOO_MANY_RECORDS + ")");
		
		currentLine = new AtomicInteger(0);
		
		PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		debug = pref.getBoolean(PreferenceConstants.ID_DEBUG_MODE);
	}

	@Override
	protected boolean startPainting(int linesToPaint, int numThreads, boolean changedBounds) {
		return true;
	}

	@Override
	public int getNumberOfLines() {
		return numLines;
	}

	@Override
	protected BaseTimelineThread getTimelineThread(ISpaceTimeCanvas canvas, double xscale,
			double yscale, Queue<TimelineDataSet> queue, IProgressMonitor monitor) {

		return new TimelineThread(controller, 
							      changedBounds,   
							      yscale, 
							      queue, 
							      monitor);
	}

	@Override
	protected void launchDataGettingThreads(boolean changedBounds,
			int numThreads) throws IOException {
		controller.startTrace(numLines, changedBounds);
	}

	@Override
	protected BasePaintThread getPaintThread(
			Queue<TimelineDataSet> queue, 
			int numLines, 
			int width, 
			IProgressMonitor monitor) {

		return new DetailPaintThread(
				device, 
				controller, 
				queue, 
				numLines, 
				currentLine, 
				width, 
				maxTextSize, 
				debug, 
				monitor);
	}

	@Override
	protected void drawPainting(ISpaceTimeCanvas canvas,
								ImagePosition imagePosition) {

		final double pixelsPerRank = canvas.getScalePixelsPerRank();
		
		DetailImagePosition imgDetailLine = (DetailImagePosition)imagePosition;
		double yscale = Math.max(pixelsPerRank, 1);

		int yposition = (int) Math.round(imgDetailLine.position * yscale);

		// put the image onto the canvas
		masterGC.drawImage(imgDetailLine.image, 0, yposition);
		origGC.drawImage(imgDetailLine.imageOriginal, 0, imgDetailLine.position);

		imgDetailLine.image.dispose();
		imgDetailLine.imageOriginal.dispose();
	}

	@Override
	protected void endPainting(boolean isCanceled) { /* print log ? */ }

	@Override
	protected void endPreparationThread(BaseTimelineThread thread, int result) {
		if (!debug)
			return;
		
		var m = thread.getInvalidData();
		if (m == null || m.size()==0)
			return;
		
		final IdTupleType idTupleType = controller.getBaseData().getIdTupleTypes();
		
		m.forEach((k,v) -> {
			System.out.println(k.toString(idTupleType) + " has invalid cpid: " + v);
		});

	}

	@Override
	protected void endDataPreparation(int numInvalidData) {

		if (numInvalidData > 0) {
			final String message = "Warning: " + numInvalidData + 
					" sample(s) have invalid call-path ID.";
			canvas.setMessage(message);
		}
	}
}
