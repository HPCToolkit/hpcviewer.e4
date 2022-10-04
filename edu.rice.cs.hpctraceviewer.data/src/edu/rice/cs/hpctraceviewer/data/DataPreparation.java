package edu.rice.cs.hpctraceviewer.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.graphics.Color;

import edu.rice.cs.hpcdata.util.ICallPath.ICallPathInfo;
import edu.rice.cs.hpctraceviewer.config.TracePreferenceManager;
import edu.rice.cs.hpcdata.experiment.scope.Scope;


/***********************************************************************
 * 
 * Basic abstract class to prepare data for trace view and depth view
 * 
 * we will use an abstract method to finalize the data preparation since 
 * 	depth view has slightly different way to paint compared to
 * 	trace view
 * 
 ***********************************************************************/
public abstract class DataPreparation
{
	protected final DataLinePainting data;
	private final HashMap<Integer, Integer>mapInvalidData;
	private final List<Integer> listInvalidData;
	
	/****
	 * Abstract class constructor to paint a line (whether it's detail view or depth view) 
	 * @param data a DataLinePainting object
	 */
	protected DataPreparation(DataLinePainting data)
	{
		this.data = data;
		mapInvalidData = new HashMap<>();
		listInvalidData = new ArrayList<>();
	}
	
	/**Painting action
	 * @return zero if everything works just fine,<br/>
	 * otherwise number of invalid cpid in case of corrupt data
	 * */
	public int collect()
	{
		if (data.depth < 0) // eclipse Linux bug: it's possible to force the depth to be negative by typing a character on the table 
			return 0;
		
		int succSampleMidpoint = (int) Math.max(0, (data.ptl.getTime(0)-data.begTime)/data.pixelLength);

		ICallPathInfo pathInfo = data.ptl.getCallPathInfo(0);

		// issue #15: Trace view doesn't render GPU trace line
		// This happens when the first sample to render has a bad cpid which causes this method to exit prematurely.
		// We shouldn't exit. Instead, we should search for the next available callpath, unless we are at 
		// the end of the data trace line
		
		if (pathInfo==null && data.ptl.size()==0)
			return 0;
		
		// issue #15: Trace view doesn't render GPU trace line
		// find the next first available data
		// If there's no data available, we exit
		
		int i=1;
		for(;i<data.ptl.size() && pathInfo == null; i++) {
			pathInfo = data.ptl.getCallPathInfo(i);
		}
		if (pathInfo == null)
			return 0;
		
		var scope = pathInfo.getScopeAt(data.depth);
		if (scope == null)
			throw new RuntimeException("Scope not found: cannot find at depth " + data.depth);
		
		final boolean gpuTrace  = data.ptl.isGPU();
		final boolean exposeGPU = TracePreferenceManager.getGPUTraceExposure() && gpuTrace;
		
		Scope  prevScope = null;
		String succFunction = scope.getName(); 
		
		Color succColor    = data.colorTable.getColor(succFunction);
		int lastIndexPTL = data.ptl.size() - 1;
		int numInvalidCP = 0;

		for (int index = 0; index < data.ptl.size(); index++)
		{
			// in case of bad cpid, we just quit painting the view
			if (pathInfo == null) {				
				// throwing an exception is more preferable, but it will make
				// more complexity to handle inside a running thread
				return numInvalidCP;		
			}

			final int currDepth = pathInfo.getMaxDepth(); 
			int currSampleMidpoint = succSampleMidpoint;
			
			//-----------------------------------------------------------------------
			// skipping if the successor has the same color and depth
			//-----------------------------------------------------------------------
			boolean stillTheSameColor = true;
			int indexSucc = index;
			int end = index;

			final Color currColor = succColor;
			final String procName = succFunction;
			final Scope currScope = scope;
			
			while (stillTheSameColor && (++indexSucc <= lastIndexPTL))
			{
				pathInfo = data.ptl.getCallPathInfo(indexSucc);
				if(pathInfo != null)
				{
					scope = pathInfo.getScopeAt(data.depth);
					if (scope == null)
						throw new RuntimeException("Scope not found: cannot find at depth " + data.depth);
					
					succFunction = scope.getName(); 
					succColor = data.colorTable.getColor(succFunction);
					
					// the color will be the same if and only if the two regions have the save function name
					// regardless they are from different max depth and different call path.
					// This can be misleading, but at the moment it is a good approximation
					
					// laksono 2012.01.23 fix: need to add a max depth condition to ensure that the adjacent
					//						   has the same depth. In depth view, we don't want to mix with
					//							different depths
					
					stillTheSameColor = (succColor.equals(currColor)) && currDepth == pathInfo.getMaxDepth();
					if (stillTheSameColor)
						end = indexSucc;
				} else {
					int cpid = data.ptl.getContextId(indexSucc);
					Integer num = mapInvalidData.get(cpid);
					if (num == null) {
						listInvalidData.add(cpid);
						num = 0;
					}
					num++;
					numInvalidCP++;
					mapInvalidData.put(cpid, num);
				}
			}
			
			if (end < lastIndexPTL)
			{
				// --------------------------------------------------------------------
				// start and middle samples: the rightmost point is the midpoint between
				// 	the two samples
				// -------------------------------------------------------------------
				// laksono 2014.11.04: previously midpoint(ptl.getTime(end),ptl.getTime(end+1)) : ptl.getTime(end);
				
				// assuming a range of three samples p0, p1 and p2 where p0 is the beginning, p1 is the last sample
				//		that has the same color as p0, and p2 is the sample that has different color as p0 (and p1)
				//		in time line: p0 < p1 < p2
				// a non-midpoint policy then should have a range of p0 to p2 with p0 color.
				
				double succ = data.usingMidpoint ? 
										midpoint(data.ptl.getTime(end), data.ptl.getTime(end+1)) :
										data.ptl.getTime(end+1);
				succSampleMidpoint = (int) Math.max(0, ((succ-data.begTime)/data.pixelLength));
			}
			else
			{
				// --------------------------------------------------------------------
				// for the last iteration (or last sample), we don't have midpoint
				// 	so the rightmost point will be the time of the last sample
				// --------------------------------------------------------------------
				// succSampleMidpoint = (int) Math.max(0, ((ptl.getTime(index+1)-begTime)/pixelLength)); 
				// johnmc: replaced above because it doesn't seem correct
				succSampleMidpoint = (int) Math.max(0, ((data.ptl.getTime(end)-data.begTime)/data.pixelLength)); 
			}
			// if we want to expose the GPU trace,
			// AND the length of the pixel is zero,
			// AND the current pixel it NOT a "idle context"
			// AND the previous pixel is a "idle context"
			// then we should extend the width of the pixel by decrementing the starting pixel
			if (exposeGPU && currSampleMidpoint == succSampleMidpoint && !currScope.isIdle()) {
				if (prevScope == null || prevScope.isIdle()) {
					currSampleMidpoint--;
				}
			}
			finishLine(procName, currSampleMidpoint, succSampleMidpoint, currDepth, currColor, end - index + 1);
			index = end;
			prevScope = currScope;
		}
		return numInvalidCP;
	}
	
	
	public List<Integer> getInvalidData() {
		return listInvalidData;
	}
	

	/**
	 * Returns the midpoint between x1 and x2
	 * 
	 * @apiNote This is potentially vulnerable to overflows but I think we are safe for now.
	 * 
	 * @param x1 
	 * @param x2
	 * */
	private static long midpoint(long x1, long x2)
	{
		return (x1 + x2)/2;
	}

	public abstract TimelineDataSet getList();

	/***
	 * Abstract method to finalize the painting given its range, depth and the function name
	 * 
	 * @param currSampleMidpoint : current sample
	 * @param succSampleMidpoint : next sample
	 * @param currDepth : current depth
	 * @param functionName : name of the function (for coloring purpose)
	 * @param sampleCount : the number of "samples"
	 */
	public abstract void finishLine(String proc, int currSampleMidpoint, int succSampleMidpoint, int currDepth, Color color, int sampleCount);
}
