package edu.rice.cs.hpctraceviewer.ui.blamestat;

import java.util.List;
import java.util.Map.Entry;
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
	private final IEventBroker eventBroker;
	private float cpuTotalBlame;

	private ColorTable colorTable;
	private SpaceTimeDataController dataTraces; 
	
	private TreeMap<Integer, TreeMap<Integer, Integer>> cpu_active_routines; // foreach_rank: (pixel: active_count)
	private TreeMap<Integer, Integer> cpu_active_count;
	
	private TreeMap<Integer, Integer> gpu_active_count;
	private TreeMap<Integer, Integer> gpu_idle_count;

	
	
	private void addDict(TreeMap<Integer, TreeMap<Integer, Integer>> dict, int key_rank, int key_pixel, int value) {
		
		if(dict.containsKey(key_rank)) {
			TreeMap<Integer, Integer> entry = dict.get(key_rank);
			
			if(entry.containsKey(key_pixel))
				entry.put(key_pixel, entry.get(key_pixel) + value);
			else
				entry.put(key_pixel, value);
			
		}else {
			
			TreeMap<Integer, Integer> entry = new TreeMap<Integer, Integer>();
			entry.put(key_pixel, value);
			dict.put(key_rank, entry);			
		}
	}
	
	
	private void addDict(TreeMap<Integer, Integer> dict, int key, int value) {
		
		if(dict.containsKey(key)) {
			dict.put(key, dict.get(key) + value);
		}else {
			dict.put(key, value);
		}
	}
	
	
	private void addDict(TreeMap<Integer, Float> dict, int key, float value) {
		
		if(dict.containsKey(key)) {
			dict.put(key, dict.get(key) + value);
		}else {
			dict.put(key, value);
		}
	}
	
	
	/****
	 * Constructor of the class
	 * @param eventBroker {@code IEventBroker}
	 */
	public CpuBlameAnalysis(IEventBroker eventBroker) {
		this.eventBroker = eventBroker;
		
		cpuBlameMap = new TreeMap<Integer, Float>();
		cpuTotalBlame = 0;
	}
	
	@Override
	public void analysisInit(SpaceTimeDataController dataTraces, ColorTable colorTable) {
		this.colorTable = colorTable;
		this.dataTraces = dataTraces;
		
				
		cpu_active_routines = new TreeMap<Integer, TreeMap<Integer, Integer>>();
		cpu_active_count = new TreeMap<Integer,Integer>();
		
		gpu_active_count = new TreeMap<Integer,Integer>();
		gpu_idle_count = new TreeMap<Integer,Integer>();
		
		cpuBlameMap.clear();		
		cpuTotalBlame = (float) 0;

	}
	
	
	@Override
	public void analysisPixelInit(int x) {
		
		cpu_active_routines.clear();
		cpu_active_count.clear();
		gpu_active_count.clear();				
		gpu_idle_count.clear();		
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
		if (process >= listTuples.size()) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("bug detected: access to " + process + " out of " + listTuples.size());
		}
		
		IdTuple tag = listTuples.get(process);
		int rank = (int) tag.getIndex(IdTupleType.KIND_RANK);
				
		isCpuThread = !tag.hasKind(IdTupleType.KIND_GPU_CONTEXT);

		RGB rgb = detailData.palette.getRGB(pixelValue);
		String proc_name = colorTable.getProcedureNameByColorHash(rgb.hashCode());

		if (isCpuThread) { // cpu thread
			if (!proc_name.equals(ColorTable.UNKNOWN_PROCNAME)) {	
				
				addDict(cpu_active_routines, rank, pixelValue, 1);
				addDict(cpu_active_count, rank, 1);
			}

		} else {		// gpu thread
			if (proc_name.equals(ColorTable.UNKNOWN_PROCNAME) ||
					proc_name.equals(GPU_SYNC)) {
								
				addDict(gpu_idle_count, rank, 1);
			}else {				
				addDict(gpu_active_count, rank, 1);
			}
		}
	}

	@Override
	public void analysisPixelFinal(int pixel) {
				
		for (Entry<Integer, TreeMap<Integer, Integer>> rank_entry : cpu_active_routines.entrySet()) {
						
			// If any gpu is idle, put blame on current active cpu routine 
			if ( !gpu_active_count.containsKey(rank_entry.getKey()) && rank_entry.getValue().containsKey(pixel)) {
				// Blame CPU
				Integer active_cpu_one = rank_entry.getValue().get(pixel);
				Integer active_cpu_all = cpu_active_count.get(rank_entry.getKey());
				
				float blameCount = active_cpu_one / (float) active_cpu_all;								
				cpuTotalBlame += blameCount;
				
				addDict(cpuBlameMap, pixel, blameCount);			
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
