// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.blamestat;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.color.ColorTable;
import edu.rice.cs.hpctraceviewer.data.color.ProcedureColor;
import edu.rice.cs.hpctraceviewer.ui.base.IPixelAnalysis;
import edu.rice.cs.hpctraceviewer.ui.internal.TraceEventData;
import edu.rice.cs.hpctraceviewer.ui.summary.SummaryData;
import edu.rice.cs.hpctraceviewer.ui.util.IConstants;
import edu.rice.cs.hpcdata.util.Constants;

/************************************************************
 * 
 * CPU-GPU blame analysis
 * 
 ************************************************************/
public class CpuBlameAnalysis implements IPixelAnalysis 
{
	private static final String GPU_SYNC = "<gpu sync>";
	
	private final TreeMap<Integer /* pixel */, Float /* percent */ >  cpuBlameMap;
	private final IEventBroker eventBroker;
	private float cpuTotalBlame;

	private ColorTable colorTable;
	private SpaceTimeDataController dataTraces; 
	
	private Map<Integer, Map<Integer, Integer>> cpu_active_routines; // foreach_rank: (pixel: active_count)
	private Map<Integer, Integer> cpu_active_count;
	
	private Map<Integer, Integer> gpu_active_count;
	private Map<Integer, Integer> gpu_idle_count;

	
	private void addDict(Map<Integer, Map<Integer, Integer>> dict, int key_rank, int key_pixel, int value) {
		
		if(dict.containsKey(key_rank)) {
			Map<Integer, Integer> entry = dict.get(key_rank);
			
			if(entry.containsKey(key_pixel))
				entry.put(key_pixel, entry.get(key_pixel) + value);
			else
				entry.put(key_pixel, value);
			
		}else {
			
			Map<Integer, Integer> entry = new HashMap<>();
			entry.put(key_pixel, value);
			dict.put(key_rank, entry);			
		}
	}
	
	
	private void addDict(Map<Integer, Integer> dict, int key, int value) {
		
		if(dict.containsKey(key)) {
			dict.put(key, dict.get(key) + value);
		} else {
			dict.put(key, value);
		}
	}
	
	
	private void addDict(Map<Integer, Float> dict, int key, float value) {
		
		if(dict.containsKey(key)) {
			dict.put(key, dict.get(key) + value);
		} else {
			dict.put(key, value);
		}
	}
	
	
	/****
	 * Constructor of the class
	 * @param eventBroker {@code IEventBroker}
	 */
	public CpuBlameAnalysis(IEventBroker eventBroker) {
		this.eventBroker = eventBroker;
		
		cpuBlameMap = new TreeMap<>();
		cpuTotalBlame = 0;
	}
	
	@Override
	public void analysisInit(SpaceTimeDataController dataTraces, 
							 ColorTable colorTable) {
		this.colorTable = colorTable;
		this.dataTraces = dataTraces;
				
		cpu_active_routines = new HashMap<>();
		cpu_active_count    = new HashMap<>();
		
		gpu_active_count = new HashMap<>();
		gpu_idle_count   = new HashMap<>();
		
		cpuBlameMap.clear();		
		cpuTotalBlame = 0;

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

		dataTraces.getTraceDisplayAttribute();

		// get the profile of the current pixel
		final var ptl = dataTraces.getTraceline(y);
		if (ptl == null)
			// hack: sometimes the summary view is still have the old image
			// and it isn't sync with the current process time line
			return;
		
		boolean isCpuThread = true;
		
		IdTuple tag = ptl.getProfileIdTuple();
		int rank = (int) Math.max(0, tag.getIndex(IdTupleType.KIND_RANK));
				
		isCpuThread = !tag.isGPU(dataTraces.getExperiment().getIdTupleType());

		RGB rgb = detailData.palette.getRGB(pixelValue);
		ProcedureColor procColor = colorTable.getProcedureNameByColorHash(rgb.hashCode());

		// special case issue #287
		if (procColor == null)
			return;
		
		String procName = procColor.getProcedure();
		
		if (isCpuThread) { // cpu thread
			if (!procColor.getProcedure().contains(Constants.PROC_NO_ACTIVITY)) {
				addDict(cpu_active_routines, rank, pixelValue, 1);
				addDict(cpu_active_count, rank, 1);
			}

		} else {		// gpu thread
			if (procName.contains(Constants.PROC_NO_ACTIVITY) ||
					procName.contains(GPU_SYNC)) {
								
				addDict(gpu_idle_count, rank, 1);
			}else {				
				addDict(gpu_active_count, rank, 1);
			}
		}
	}

	@Override
	public void analysisPixelFinal(int pixel) {
				
		for (Entry<Integer, Map<Integer, Integer>> rank_entry : cpu_active_routines.entrySet()) {
						
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

		TraceEventData eventData = new TraceEventData(dataTraces, this, data);

		eventBroker.post(IConstants.TOPIC_BLAME, eventData);				
	}
}
