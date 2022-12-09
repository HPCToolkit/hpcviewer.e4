package edu.rice.cs.hpcdata.db.version4;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

/*******************************************************************************************
 * 
 * Class to manage cct.db (aka plot.db in older version) file
 * <p>See {@link getPlotEntry} to get the list of plot data</p>
 *
 *******************************************************************************************/
public class DataPlot extends DataCommon 
{
	public  static final String FILE_CCT_DB = "cct.db";
	private static final String HEADER   = "HPCTOOLKITctxt";
	
	/*** list of cct. In the future we may need to implement with concurrent list.
	 *** Right now it's just a simple array or list. Please use it carefully   
	 ***/
	
	private ContextInfo []listContexts;
	
	
	//////////////////////////////////////////////////////////////////////////
	// Override methods from DataCommon
	//////////////////////////////////////////////////////////////////////////

	@Override
	public void open(final String filename)
			throws IOException
	{
		super.open(filename + File.separator + FILE_CCT_DB);
	}
	
	@Override
	public void dispose() throws IOException {
		listContexts = null;
		super.dispose();
	}

	private static final int NUM_ITEMS = 1;

	@Override
	protected int getNumSections() {
		return NUM_ITEMS;
	}


	@Override
	protected boolean isFileHeaderCorrect(String header) {
		return header.equals(HEADER);
	}


	@Override
	protected boolean isFileFooterCorrect(String header) {
		return header.equals("__ctx.db");
	}

	
	@Override
	protected boolean readNextHeader(FileChannel input, DataSection []sections) throws IOException {
		
		ByteBuffer buffer = input.map(MapMode.READ_ONLY, sections[0].offset, sections[0].size);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		long pCtx = buffer.getLong();
		int  nCtx = buffer.getInt(0x08);
		byte size = buffer.get(0x0c);
		long basePosition = pCtx - sections[0].offset;

		listContexts = new ContextInfo[nCtx];

		for(int i=0; i<nCtx; i++) {
			int position   = (int)(basePosition + (i*size));
			
			long nValues   = buffer.getLong (position);
			long pValues   = buffer.getLong (position + 0x08);
			short nMetrics = buffer.getShort(position + 0x10);
			long  pIndices = buffer.getLong (position + 0x18);
			
			var info = new ContextInfo(nValues, pValues, nMetrics, pIndices);
			listContexts[i] = info;
		}
		return true;
	}

	
	@Override
	public void printInfo( PrintStream out)
	{
		super.printInfo(out);
		
		// reading some parts of the indexes
		for(int j=0; j<listContexts.length; j++)
		{
			ContextInfo ctxInfo = listContexts[j];
			short numMetrics = ctxInfo.nMetrics;

			out.format("[cct %5d] ", j);
			out.println(ctxInfo);
			
			for(short i=0; i<numMetrics; i++) {
				out.print("\t m: " + i);
				try {
					DataPlotEntry []entries = getPlotEntry(ctxInfo, i);
					for (DataPlotEntry entry: entries) {
						out.print(" " + entry);
					}
					out.println();
				} catch (IOException e) {
					out.print(e.getMessage());
				}
	
			}
		}
	}
	
	
	//////////////////////////////////////////////////////////////////////////
	// public methods
	//////////////////////////////////////////////////////////////////////////

	
	/***
	 * Retrieve a plot entry given a cct id
	 * 
	 * @param cct the CCT id
	 * @param metric the metric "raw" id, it is NOT the metric id. This "raw" id starts from zero.
	 * @return array of plot data entry if exists, null otherwise.
	 * 
	 * @throws IOException
	 */
	public DataPlotEntry []getPlotEntry(int cct, int metric) throws IOException
	{
		ContextInfo info = listContexts[cct];
		return getPlotEntry(info, metric);
	}
	
	/******
	 * Retrieve a list of plot data of a given cct index and metric raw index
	 *  
	 * @param cct : cct index
	 * @param metric : the index of the raw metric
	 * 
	 * @return an array of plot data entry containing the value if exist. Null otherwise.
	 * 
	 * @throws IOException
	 */
	private DataPlotEntry []getPlotEntry(ContextInfo info, int metric) throws IOException
	{
		if (info == null)
			return new DataPlotEntry[0];

		final int FMT_CCTDB_SZ_MIdx = 0x0a;
		final int FMT_CCTDB_SZ_PVal = 0x0c;
		
		var channel = getChannel();
		var bufSize = (info.nMetrics * FMT_CCTDB_SZ_MIdx) + info.pMetricIndices - info.pValues;
		var buffer  = channel.map(MapMode.READ_ONLY, info.pValues, bufSize);
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		var basePosition =  info.pMetricIndices - info.pValues;

		// Linear search of metric O(n)
		// if n (number of non-zero metrics) is huge, we are in trouble
		for(var i=0; i<info.nMetrics; i++) {
			// Get {Idx} start 
			// 00: 	u16 	metricId 	4.0 	Unique identifier of a metric listed in the meta.db
			// 02: 	u64 	startIndex 	4.0 	Start index of *pValues from the associated metric
			int position  = (int) (basePosition + (i * FMT_CCTDB_SZ_MIdx));
			short metricId = buffer.getShort(position);
			
			if (metricId != metric)
				continue;
			
			int startIdx = (int)buffer.getLong(position + 0x02);			
			int endIdx   = (int)info.nValues;

			// Get {Idx} next or end 			
			if (i+1 < info.nMetrics) {
				position = (int) (basePosition + ((i+1) * FMT_CCTDB_SZ_MIdx));
				endIdx = (int)buffer.getLong(position + 0x02);
			}
			
			int numValues = (endIdx-startIdx);
			DataPlotEntry []values = new DataPlotEntry[numValues];
			basePosition = 0;
			
			// Read all metric values of this cct
			// 00: 	u32 	profIndex 	4.0 	Index of a profile listed in the profile.db
			// 04: 	f64 	value 	    4.0 	Value attributed to the profile indicated by profIndex
			for (var j=startIdx; j<endIdx; j++) {
				position = (int) (basePosition + j * FMT_CCTDB_SZ_PVal);
				int profIndex = buffer.getInt(position);
				double value  = buffer.getDouble(position + 0x04);

				values[j-startIdx] = new DataPlotEntry(profIndex, value);
			}
			return values;
		}		
		return new DataPlotEntry[0];
	}
	
	
	//////////////////////////////////////////////////////////////////////////
	// Private classes
	//////////////////////////////////////////////////////////////////////////

	private static class ContextInfo
	{
		private final long  nValues;
		private final long  pValues;
		private final short nMetrics;
		private final long  pMetricIndices;
		
		public ContextInfo(long  nValues, long  pValues, short nMetrics, long pMetricIndices) {
			this.nValues  = nValues;
			this.pValues  = pValues;
			this.nMetrics = nMetrics;
			this.pMetricIndices = pMetricIndices;
		}
		
		public String toString() {
			return String.format("pV: 0x%x, nV: %d, pM: 0x%x, nM: %s", pValues, nValues, pMetricIndices, nMetrics);
		}
	}
}
