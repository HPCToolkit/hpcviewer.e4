package edu.rice.cs.hpctraceviewer.ui.blamestat;

import java.util.List;
import java.util.TreeMap;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.cs.hpc.data.db.IdTuple;
import edu.rice.cs.hpc.data.db.IdTupleType;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.IFileDB.IdTupleOption;
import edu.rice.cs.hpctraceviewer.data.ColorTable;
import edu.rice.cs.hpctraceviewer.data.ImageTraceAttributes;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.ui.base.IPixelAnalysis;
import edu.rice.cs.hpctraceviewer.ui.summary.SummaryData;
import edu.rice.cs.hpctraceviewer.ui.util.IConstants;


/************************************************************
 * 
 * CPU-GPU blame analysis
 * 
 ************************************************************/
public class CpuBlameAnalysis implements IPixelAnalysis 
{
	private final static String GPU_SYNC = "<gpu sync>";
	
	private final TreeMap<Integer /* pixel */, Float /* percent */ >  cpuBlameMap;
	private final TreeMap<Integer, Integer> mapCpuPixelCount;
	private final IEventBroker eventBroker;
	private float cpuTotalBlame;

	private ColorTable colorTable;
	private SpaceTimeDataController dataTraces; 
	
	private int cpu_active_count = 0;
	private int gpu_active_count = 0;
	private int gpu_idle_count = 0;
	private int cpu_idle_count = 0;

	
	/****
	 * Constructor of the class
	 * @param eventBroker {@code IEventBroker}
	 */
	public CpuBlameAnalysis(IEventBroker eventBroker) {
		this.eventBroker = eventBroker;
		
		cpuBlameMap      = new TreeMap<Integer, Float>();
		mapCpuPixelCount = new TreeMap<Integer, Integer>();
		new TreeMap<Integer, Integer>();
		
		cpuTotalBlame = 0;
	}
	
	@Override
	public void analysisInit(SpaceTimeDataController dataTraces, ColorTable colorTable) {
		this.colorTable = colorTable;
		this.dataTraces = dataTraces;
		
		cpuBlameMap.clear();		
		cpuTotalBlame = (float) 0;

	}

	@Override
	public void analysisPixelInit(int x) {
		cpu_active_count = 0;
		gpu_active_count = 0;
		gpu_idle_count = 0;
		cpu_idle_count = 0;
		
		mapCpuPixelCount.clear();
	}

	@Override
	public void analysisPixelXY(ImageData detailData, int x, int y, int pixelValue) {

		// Blame-Shift init table for the current callstack level
		final ImageTraceAttributes attributes = dataTraces.getAttributes();
		
        final IBaseData traceData = dataTraces.getBaseData();
        
        // get the list of id tuples
		List<IdTuple> listTuples = traceData.getListOfIdTuples(IdTupleOption.BRIEF);

		// get the profile of the current pixel
		int process = attributes.convertTraceLineToRank(y);				

		boolean isCpuThread = true;

		// get the profile's id tuple and verify if the later is a cpu thread
		if (process < listTuples.size()) {
			IdTuple tag = listTuples.get(process);
			isCpuThread = !tag.hasKind(IdTupleType.KIND_GPU_CONTEXT);
		} else {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("bug detected: access to " + process + " out of " + listTuples.size());
		}

		RGB rgb = detailData.palette.getRGB(pixelValue);
		String proc_name = colorTable.getProcedureNameByColorHash(rgb.hashCode());

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
					proc_name.equals(GPU_SYNC)) {

				gpu_idle_count = gpu_idle_count + 1;
			} else {
				gpu_active_count = gpu_active_count + 1;
			} 
		}
	}

	@Override
	public void analysisPixelFinal(int pixel) {
		
		// If all gpu is idle, we compute the blame to cpu.
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

	}

	@Override
	public void analysisFinal(ImageData detailData) {

		SummaryData data = new SummaryData(	detailData.palette, colorTable, 
								cpuBlameMap, cpuTotalBlame,
								null, 0);
		
		eventBroker.post(IConstants.TOPIC_BLAME, data);				
	}
}
