package edu.rice.cs.hpctraceviewer.ui.summary;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.eclipse.swt.graphics.PaletteData;
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
public class SummaryTimeCanvas extends AbstractTimeCanvas implements IOperationHistoryListener {
	private static final boolean True = false;
	private final IEventBroker eventBroker;
	private final ITracePart tracePart;

	private SpaceTimeDataController dataTraces = null;
	private TreeMap<Integer /* pixel */, Integer /* percent */> mapPixelToPercent;
	
	
	private TreeMap<Integer /* callstack_level */, TreeMap<Integer /* pixel */, Float /* percent */ >> cpuBlameMap;
	private TreeMap<Integer /* callstack_level */, TreeMap<Integer /* pixel */, Float /* percent */ >> gpuBlameMap;
	
	private TreeMap<Integer /* callstack_level */, Float> cpuTotalBlame;
	private TreeMap<Integer /* callstack_level */, Float> gpuTotalBlame;
	
	private int totPixels;
	private ImageData detailData;
//	private final int whitePixel;
	
	/**********************************
	 * Construct a summary canvas without background nor scrollbar
	 * 
	 * @param composite
	 **********************************/
	public SummaryTimeCanvas(ITracePart tracePart, Composite composite, IEventBroker eventBroker) {
		super(composite, SWT.NO_BACKGROUND);

		this.eventBroker = eventBroker;
		this.tracePart = tracePart;
		tracePart.getOperationHistory().addOperationHistoryListener(this);
		this.cpuBlameMap = new TreeMap<Integer, TreeMap<Integer, Float>>();
		this.gpuBlameMap = new TreeMap<Integer, TreeMap<Integer, Float>>();
		this.cpuTotalBlame = new TreeMap<Integer, Float>();
		this.gpuTotalBlame = new TreeMap<Integer, Float>();
		
//		this.whitePixel = detailData.palette.getPixel(new RGB(255,255,255));
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

	private void putBlameCpu(Integer depth, Integer pixel, Float blame) {

		if (cpuBlameMap.containsKey(depth)) {
			TreeMap<Integer, Float> cur_map = cpuBlameMap.get(depth);

			cpuTotalBlame.put(depth, cpuTotalBlame.get(depth) + blame);
			
			if (cur_map.containsKey(pixel)) {
				cur_map.put(pixel, cur_map.get(pixel) + blame);
			} else {
				cur_map.put(pixel, blame);
			}
		}
	}

	private void putBlameGpu(Integer depth, Integer pixel, Float blame) {

		if (gpuBlameMap.containsKey(depth)) {
			TreeMap<Integer, Float> cur_map = gpuBlameMap.get(depth);

			gpuTotalBlame.put(depth, gpuTotalBlame.get(depth) + blame);
			
			if (cur_map.containsKey(pixel)) {
				cur_map.put(pixel, cur_map.get(pixel) + blame);
			} else {
				cur_map.put(pixel, blame);
			}
		}
	}
	
	private void incrementPixelCount(TreeMap<Integer, Integer> mapPixelToCount, Integer pixelValue) {
		Integer count = mapPixelToCount.get(pixelValue);
		if (count != null)
			mapPixelToCount.put(pixelValue, count + 1);
		else
			mapPixelToCount.put(pixelValue, 1);
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

		mapPixelToPercent = new TreeMap<Integer, Integer>();

		// Blame-Shift init table for the current callstack level
		final ImageTraceAttributes attributes = dataTraces.getAttributes();
		Integer cs_depth = attributes.getDepth();
		
        final IBaseData traceData = dataTraces.getBaseData();
        
        // get the list of id tuples
		List<IdTuple> listTuples = traceData.getListOfIdTuples(IdTupleOption.BRIEF);
		
		
		if (cpuBlameMap.containsKey(cs_depth) == false ) {//&& dataTraces.isHomeView() == true) {
			cpuBlameMap.put(cs_depth, new TreeMap<Integer, Float>());
			gpuBlameMap.put(cs_depth, new TreeMap<Integer, Float>());
			
			cpuTotalBlame.put(cs_depth, (float) 0);
			gpuTotalBlame.put(cs_depth, (float) 0);
		}else {
			cpuBlameMap.get(cs_depth).clear();
			gpuBlameMap.get(cs_depth).clear();
			
			cpuTotalBlame.put(cs_depth, (float) 0);
			gpuTotalBlame.put(cs_depth, (float) 0);
		}


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
			TreeMap<Integer, Integer> cpuPixelCount = new TreeMap<Integer, Integer>();
			TreeMap<Integer, Integer> gpuPixelCount = new TreeMap<Integer, Integer>();
			
			Integer cpu_active_count = 0;
			Integer gpu_active_count = 0;
			Integer gpu_idle_count = 0;
			Integer cpu_idle_count = 0;
						
			for (int y = 0; y < detailData.height; ++y) { // One iter per trace line
								
				// get the profile of the current pixel
				int process = attributes.convertTraceLineToRank(y);
				
				int pixelValue = detailData.getPixel(x, y);
				
				
				// get the profile's id tuple
				IdTuple tag = listTuples.get(process);
				// check if the profile is a thread
				boolean isCpuThread = tag.hasKind(IdTupleType.KIND_THREAD);
				RGB rgb = detailData.palette.getRGB(pixelValue);
				String proc_name = getColorTable().getProcedureNameByColorHash(rgb.hashCode());
							
				
				if (isCpuThread) { // cpu thread
					if (proc_name.equals("[No activity]") || proc_name.equals("<no activity>")) {
						cpu_idle_count = cpu_idle_count + 1;
					}else {
						cpu_active_count = cpu_active_count + 1;
						incrementPixelCount(cpuPixelCount, pixelValue);
					}
					
				} else {		// gpu thread
					if (proc_name.equals("[No activity]") || proc_name.equals("<no activity>")) {
						gpu_idle_count = gpu_idle_count + 1;
					}else if(proc_name.equals("<gpu sync>")){
						gpu_idle_count = gpu_idle_count + 1;
						incrementPixelCount(gpuPixelCount, pixelValue);
					}else {
						gpu_active_count = gpu_active_count + 1;
						incrementPixelCount(gpuPixelCount, pixelValue);
					}
				}	
				
			}
			
			// Blame analysis
			if (cpu_active_count > 0 && gpu_active_count == 0 && gpu_idle_count != 0) {
				// Blame CPU
				for(Map.Entry<Integer,Integer> entry : cpuPixelCount.entrySet()) {					
					putBlameCpu(cs_depth, entry.getKey(), (float) entry.getValue() / cpu_active_count);					
				}
									
			} else if (gpu_active_count > 0 && cpu_active_count == 0 && cpu_idle_count != 0) {
				// Blame GPU
//				for(Map.Entry<Integer,Integer> entry : gpuPixelCount.entrySet()) {					
//					putBlameGpu(cs_depth, entry.getKey(), (float) entry.getValue() / gpu_active_count);						
//				}
			}

			
			// ---------------------------------------------------------------------------
			// draw the line of a specific color with a specific length from bottom to the
			// top
			// note: the coordinates 0,0 starts from the top-left corner !
			// ---------------------------------------------------------------------------
			cpuPixelCount.putAll(gpuPixelCount);
			Set<Integer> set = cpuPixelCount.keySet();
			int yOffset = viewHeight;
			int h = 0;
			
			for (Iterator<Integer> it = set.iterator(); it.hasNext();) {
				final Integer pixel = it.next();
				final RGB rgb = detailData.palette.getRGB(pixel);

				final Color c = new Color(getDisplay(), rgb);
				final Integer numCounts = cpuPixelCount.get(pixel);

				final int height = (int) Math.ceil(numCounts * yScale);

				buffer.setBackground(c);

				// if this is the last color, we should draw from the current position to the
				// end
				// this may not be the best solution, but the round-up in height variable may
				// give
				// empty spaces if the number of colors are not a height's divisor.

				if (it.hasNext())
					buffer.fillRectangle(xOffset, yOffset - height, (int) Math.max(1, xScale), height);
				else {
					buffer.fillRectangle(xOffset, 0, (int) Math.max(1, xScale), viewHeight - h);
				}
				yOffset -= height;
				c.dispose();

				// accumulate the statistics of this pixel
				Integer val = mapPixelToPercent.get(pixel);
				Integer acc = (val == null ? numCounts : val + numCounts);
				mapPixelToPercent.put(pixel, acc);

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

		final ImageTraceAttributes attributes = dataTraces.getAttributes();
		Integer cs_depth = attributes.getDepth();

		Integer totalPixels = detailData.height * detailData.width;
		SummaryData data = new SummaryData(detailData.palette, mapPixelToPercent, getColorTable(), totalPixels);
		eventBroker.post(IConstants.TOPIC_STATISTICS, data);

		
		if (cpuBlameMap.get(cs_depth) != null) {
			data = new SummaryData(	detailData.palette, getColorTable(), 
									cpuBlameMap.get(cs_depth), cpuTotalBlame.get(cs_depth),
									gpuBlameMap.get(cs_depth), gpuTotalBlame.get(cs_depth));
			eventBroker.post(IConstants.TOPIC_BLAME, data);				
		}
	}

	// ---------------------------------------------------------------------------------------
	// Override methods
	// ---------------------------------------------------------------------------------------

	@Override
	public void historyNotification(final OperationHistoryEvent event) {
		// we are not interested with other operation
		IUndoContext context = tracePart.getContext(BaseTraceContext.CONTEXT_OPERATION_BUFFER);

		if (event.getOperation().hasContext(context)) {
			if (event.getEventType() == OperationHistoryEvent.DONE) {

				if (isDisposed())
					return;

				final IUndoableOperation operation = event.getOperation();
				if (!(operation instanceof AbstractTraceOperation)) {
					return;
				}
				AbstractTraceOperation op = (AbstractTraceOperation) operation;
				if (op.getData() != dataTraces)
					return;

				getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						BufferRefreshOperation operation = (BufferRefreshOperation) event.getOperation();
						refresh(operation.getImageData());
					}
				});
			}
		}
	}

	@Override
	protected void changePosition(Point point) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void changeRegion(Rectangle region) {
		final ImageTraceAttributes attributes = dataTraces.getAttributes();

		long timeBegin = attributes.getTimeBegin();
		int left = region.x;
		int right = region.width + region.x;

		long topLeftTime = timeBegin + (long) (left / getScalePixelsPerTime());
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