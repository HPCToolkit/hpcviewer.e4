package edu.rice.cs.hpctraceviewer.ui.summary;

import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;


import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;


import edu.rice.cs.hpc.data.db.IdTuple;
import edu.rice.cs.hpc.data.db.IdTupleType;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.IFileDB.IdTupleOption;

import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;
import edu.rice.cs.hpctraceviewer.ui.context.BaseTraceContext;
import edu.rice.cs.hpctraceviewer.ui.internal.AbstractTimeCanvas;
import edu.rice.cs.hpctraceviewer.ui.operation.AbstractTraceOperation;
import edu.rice.cs.hpctraceviewer.ui.operation.BufferRefreshOperation;
import edu.rice.cs.hpctraceviewer.ui.operation.ZoomOperation;
import edu.rice.cs.hpctraceviewer.ui.util.IConstants;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.Frame;
import edu.rice.cs.hpctraceviewer.data.ImageTraceAttributes;
import edu.rice.cs.hpctraceviewer.data.Position;
import edu.rice.cs.hpctraceviewer.data.ColorTable;
import edu.rice.cs.hpctraceviewer.data.util.Constants;

/******************************************************************
 * 
 * Canvas class for summary view
 *
 ******************************************************************/
public class SummaryTimeCanvas extends AbstractTimeCanvas implements IOperationHistoryListener 
{
	private final IEventBroker eventBroker;
	private final ITracePart tracePart;

	private SpaceTimeDataController dataTraces = null;
	private TreeMap<Integer /* pixel */, Integer /* percent */> mapPixelToPercent;	
	private TreeMap<Integer /* pixel */, Float /* percent */ >  cpuBlameMap;
	
	private float cpuTotalBlame;
	
	private int totPixels;
	private ImageData detailData;

	
	/**********************************
	 * Construct a summary canvas without background nor scrollbar
	 * 
	 * @param composite
	 **********************************/
	public SummaryTimeCanvas(ITracePart tracePart, Composite composite, IEventBroker eventBroker) {
		super(composite, SWT.NO_BACKGROUND);

		this.eventBroker = eventBroker;
		this.tracePart = tracePart;
		
		mapPixelToPercent = new TreeMap<Integer, Integer>();		
		cpuBlameMap = new TreeMap<Integer, Float>();
		cpuTotalBlame = 0;
		
		tracePart.getOperationHistory().addOperationHistoryListener(this);
	}

	@Override
	public void paintControl(PaintEvent event) {
		if (dataTraces == null)
			return;

		super.paintControl(event);
		refreshWithCondition();
	}

	@Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.
	 * DisposeEvent)
	 */
	public void widgetDisposed(DisposeEvent e) {
		tracePart.getOperationHistory().removeOperationHistoryListener(this);
	}

	private void refreshWithCondition() {
		if (getBuffer() == null) {
			// ------------------------------------------------------------------------
			// ------------------------------------------------------------------------
			rebuffer(detailData);
			return;
		}

		// ------------------------------------------------------------------------
		// we need to avoid repainting if the size of the image buffer is not the same
		// as the image of the canvas. This case happens when the view is resize while
		// it's in hidden state, and then it turns visible.
		// this will cause misalignment in the view
		// ------------------------------------------------------------------------
		final Rectangle r1 = getBuffer().getBounds();
		final Rectangle r2 = getClientArea();

		if (!(r1.height == r2.height && r1.width == r2.width)) {
			rebuffer(detailData);
			return;
		}
	}

	
	/*****
	 * rebuffers the data in the summary time canvas and then asks receiver to paint
	 * it again
	 *****/
	private void rebuffer(ImageData detailData) {
		// store the original image data for further usage such as when the painting is
		// needed.
		this.detailData = detailData;

		if (detailData == null)
			return;

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

		GC buffer = new GC(imageBuffer);
		buffer.setBackground(Constants.COLOR_WHITE);
		buffer.fillRectangle(0, 0, viewWidth, viewHeight);

		float yScale = (float) viewHeight / (float) detailData.height;
		float xScale = ((float) viewWidth / (float) detailData.width);
		int xOffset = 0;

		// Blame-Shift init table for the current callstack level
		final ImageTraceAttributes attributes = dataTraces.getAttributes();
		attributes.getDepth();
		
        final IBaseData traceData = dataTraces.getBaseData();
        
        // get the list of id tuples
		List<IdTuple> listTuples = traceData.getListOfIdTuples(IdTupleOption.BRIEF);
		
		mapPixelToPercent.clear();
		cpuBlameMap.clear();		
		cpuTotalBlame = (float) 0;

		// ---------------------------------------------------------------------------
		// needs to be optimized:
		// for every pixel along the width, check the pixel, group them based on color,
		// count the amount of each group, and draw the pixel
		// ---------------------------------------------------------------------------
		for (int x = 0; x < detailData.width; ++x) {
			// ---------------------------------------------------------------------------
			// use tree map to sort the key of color map
			// without sort, it can be confusing
			// ---------------------------------------------------------------------------
			TreeMap<Integer, Integer> mapCpuPixelCount = new TreeMap<Integer, Integer>();
			TreeMap<Integer, Integer> mapPixelToCount  = new TreeMap<Integer, Integer>();
			
			int cpu_active_count = 0;
			int gpu_active_count = 0;
			int gpu_idle_count = 0;
			int cpu_idle_count = 0;
						
			for (int y = 0; y < detailData.height; ++y) { // One iter per trace line

				int pixelValue = detailData.getPixel(x, y);
								
				// get the profile of the current pixel
				int process = attributes.convertTraceLineToRank(y);				

				// get the profile's id tuple and verify if the later is a cpu thread
				IdTuple tag = listTuples.get(process);
				boolean isCpuThread = !tag.hasKind(IdTupleType.KIND_GPU_CONTEXT);
				
				RGB rgb = detailData.palette.getRGB(pixelValue);
				String proc_name = getColorTable().getProcedureNameByColorHash(rgb.hashCode());
				
				if (isCpuThread) { // cpu thread
					if (proc_name.equals(ColorTable.UNKNOWN_PROCNAME)) {
						cpu_idle_count = cpu_idle_count + 1;
					} else {
						cpu_active_count = cpu_active_count + 1;
						Integer count = mapCpuPixelCount.get(pixelValue);
						if (count == null) {
							mapCpuPixelCount.put(pixelValue, 1);
						} else {
							mapCpuPixelCount.put(pixelValue, count+1);
						}
					}
					
				} else {		// gpu thread
					if (proc_name.equals(ColorTable.UNKNOWN_PROCNAME) ||
						proc_name.equals("<gpu sync>")) {

						gpu_idle_count = gpu_idle_count + 1;
					} else {
						gpu_active_count = gpu_active_count + 1;
					} 
				}
				Integer old = mapPixelToCount.get(pixelValue);
				if (old != null) {
					old++;
					mapPixelToCount.put(pixelValue, old);
				} else {
					mapPixelToCount.put(pixelValue, 1);
				}
			}
			
			// ---------------------------------------------------------------------------
			// draw the line of a specific color with a specific length from bottom to the
			// top
			// note: the coordinates 0,0 starts from the top-left corner !
			// ---------------------------------------------------------------------------

			int yOffset = viewHeight;
			int h = 0;
			int i = 1;
			int size = mapPixelToCount.size();
			
			for (Entry<Integer, Integer> entry: mapPixelToCount.entrySet()) {
				final Integer pixel = entry.getKey();
				final Integer count = entry.getValue();

				final RGB rgb = detailData.palette.getRGB(pixel);
				final Color c = new Color(getDisplay(), rgb);

				final int height = (int) Math.ceil(count * yScale);

				buffer.setBackground(c);

				// if this is the last color, we should draw from the current position to the end
				// this may not be the best solution, but the round-up in height variable may give
				// empty spaces if the number of colors are not a height's divisor.

				if (i==size) {
					buffer.fillRectangle(xOffset, yOffset - height, (int) Math.max(1, xScale), height);
				} else {
					buffer.fillRectangle(xOffset, 0, (int) Math.max(1, xScale), viewHeight - h);
				}
				i++;
				yOffset -= height;
				c.dispose();

				// accumulate the statistics of this pixel
				Integer val = mapPixelToPercent.get(pixel);
				Integer acc = (val == null ? count : val + count);
				mapPixelToPercent.put(pixel, acc);

				// ----------------------------------------------------------------------------
				// GPU Blame analysis:
				// If all gpu is idle, we compute the blame to cpu.
				// ----------------------------------------------------------------------------
				
				if (cpu_active_count > 0 && gpu_active_count == 0 && gpu_idle_count != 0 ) {
					// Blame CPU
					Integer blameCount = mapCpuPixelCount.get(pixel);
					if (blameCount != null) {
						
						float blame = blameCount.floatValue() / cpu_active_count;
						cpuTotalBlame = cpuTotalBlame + blame;
						Float oldBlame = cpuBlameMap.get(pixel);
						if (oldBlame != null) {
							cpuBlameMap.put(pixel, oldBlame + blame);
						} else {
							cpuBlameMap.put(pixel, blame);
						}
					}
				}
				
				h += height;
			}
			xOffset = Math.round(xOffset + xScale);
		}
		totPixels = detailData.width * detailData.height;

		buffer.dispose();

		redraw();

		broadcast(detailData);
	}

	
	/****
	 * main method to decide whether we want to create a new buffer or just to
	 * redraw the canvas
	 * 
	 * @param _detailData : new data
	 */
	private void refresh(ImageData detailData) {
		super.init();
		rebuffer(detailData);
	}

	/********
	 * set the new database
	 * 
	 * @param data
	 ********/
	public void updateData(SpaceTimeDataController data) {
		dataTraces = data;
		setVisible(true);
		redraw();
	}

	/*****
	 * get the number of pixel per time unit
	 * 
	 * @return
	 */
	private double getScalePixelsPerTime() {
		final int viewWidth = getClientArea().width;

		return (double) viewWidth / (double) getNumTimeDisplayed();
	}

	/******
	 * get the time interval displayed on the canvas
	 * 
	 * @return
	 */
	private long getNumTimeDisplayed() {
		return (dataTraces.getAttributes().getTimeInterval());
	}

	/****
	 * <p>
	 * broadcast the content of summary if it's already computed If the content is
	 * not computed, do nothing.
	 * </p>
	 */
	public void broadcast() {
		if (this.detailData != null) {
			broadcast(detailData);
		}
	}

	private void broadcast(ImageData detailData) { // PaletteData palette, int totalPixels) {

		Integer totalPixels = detailData.height * detailData.width;
		SummaryData data = new SummaryData(detailData.palette, mapPixelToPercent, getColorTable(), totalPixels);
		eventBroker.post(IConstants.TOPIC_STATISTICS, data);

		data = new SummaryData(	detailData.palette, getColorTable(), 
								cpuBlameMap, cpuTotalBlame,
								null, 0
								/*gpuBlameMap, gpuTotalBlame*/);
		
		eventBroker.post(IConstants.TOPIC_BLAME, data);				
	}

	// ---------------------------------------------------------------------------------------
	// Override methods
	// ---------------------------------------------------------------------------------------

	@Override
	public void historyNotification(final OperationHistoryEvent event) {

		if (isDisposed())
			return;

		// we are not interested with other operation contexts
		IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_BUFFER);
		if (!event.getOperation().hasContext(context)) 
			return;
		
		if (event.getEventType() == OperationHistoryEvent.DONE) {

			final IUndoableOperation operation = event.getOperation();
			if (!(operation instanceof AbstractTraceOperation)) {
				return;
			}
			final AbstractTraceOperation op = (AbstractTraceOperation) operation;
			if (op.getData() != dataTraces)
				return;

			getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					BufferRefreshOperation operation = (BufferRefreshOperation) op;
					refresh(operation.getImageData());
				}
			});
		}
	}

	@Override
	protected void changePosition(Point point) {}
	

	@Override
	protected void changeRegion(Rectangle region) {
		final ImageTraceAttributes attributes = dataTraces.getAttributes();

		long timeBegin = attributes.getTimeBegin();
		int left  = region.x;
		int right = region.width + region.x;

		long topLeftTime     = timeBegin + (long) (left  / getScalePixelsPerTime());
		long bottomRightTime = timeBegin + (long) (right / getScalePixelsPerTime());

		final Position position = attributes.getPosition();

		final Frame frame = new Frame(topLeftTime, bottomRightTime, attributes.getProcessBegin(),
				attributes.getProcessEnd(), attributes.getDepth(), position.time, position.process);

		IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_TRACE);
		try {
			tracePart.getOperationHistory().execute(new ZoomOperation(dataTraces, "Time zoom in", frame, context), null,
					null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected String tooltipText(int pixel, RGB rgb) {

		// ------------------------------------------------
		// get the number of counts of this pixel
		// ------------------------------------------------
		Integer stat = mapPixelToPercent.get(pixel);

		if (stat != null) {
			// ------------------------------------------------
			// compute the percentage
			// ------------------------------------------------
			float percent = (float) 100.0 * ((float) stat / (float) totPixels);

			if (percent > 0) {
				final String percent_str = String.format("%.2f %%\n", percent);
				return percent_str;
			}
		}
		return null;
	}

	@Override
	protected ColorTable getColorTable() {
		return dataTraces.getColorTable();
	}
}