package edu.rice.cs.hpcdata.tld.plot;

import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;

import edu.rice.cs.hpcdata.db.version3.DataCommon;
import edu.rice.cs.hpcdata.db.version3.DataSection;

/*******************************************************************************************
 * 
 * Class to manage plot.db file
 * <p>See {@link getPlotEntry} to get the list of plot data</p>
 *
 *******************************************************************************************/
public class DataPlot extends DataCommon 
{
	private static final String HEADER = "HPCPROF-cmsdb___";
	
	/*** list of cct. In the future we may need to implement with concurrent list.
	 *** Right now it's just a simple array or list. Please use it carefully   
	 ***/
	
	private List<ContextInfo> listContexts;
		
	private RandomAccessFile file;
	
	
	//////////////////////////////////////////////////////////////////////////
	// Override methods from DataCommon
	//////////////////////////////////////////////////////////////////////////

	@Override
	public void open(final String filename)
			throws IOException
	{
		super.open(filename);
		file = new RandomAccessFile(filename, "r");
	}
	
	@Override
	public void dispose() {
		listContexts = null;
		
		try {
			if (file != null)
				file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected boolean isTypeFormatCorrect(long type) {
		return type == 3;
	}

	@Override
	protected boolean isFileHeaderCorrect(String header) {
		return header.equals(HEADER);
	}

	
	@Override
	protected boolean readNextHeader(FileChannel input, DataSection []sections) throws IOException {
		if (numItems == 0)
			return false;
		
		input.position(sections[0].offset);
		
		listContexts = new ArrayList<DataPlot.ContextInfo>((int) numItems);
		
		ByteBuffer buffer = ByteBuffer.allocate((int) (numItems * ContextInfo.SIZE));
		
		int numBytes = input.read(buffer);
		if (numBytes > 0) 
		{
			buffer.flip();
			for(int i=0; i<numItems; i++) {
				ContextInfo info = new ContextInfo();
				
				info.id = buffer.getInt();
				info.numValues = buffer.getLong();
				info.numNonZeroMetrics = buffer.getShort();
				info.offset = buffer.getLong();
				
				listContexts.add(info);
			}
		}
		
		long nextPosition = sections[0].offset + getMultiplyOf8( sections[0].size);
		input.position(nextPosition);

		return true;
	}

	
	@Override
	public void printInfo( PrintStream out)
	{
		super.printInfo(out);
		
		// reading some parts of the indexes
		for(int j=0; j<listContexts.size(); j++)
		{
			ContextInfo ctxInfo = listContexts.get(j);
			short numMetrics = ctxInfo.numNonZeroMetrics;

			out.format("%5d [cct %5d] ", j, ctxInfo.id);
			System.out.println(ctxInfo);
			
			for(short i=0; i<numMetrics; i++) {
				System.out.print("\t m: " + i);
				try {
					DataPlotEntry []entries = getPlotEntry(ctxInfo, i);
					if (entries != null) {
						for (DataPlotEntry entry: entries) {
							out.print(" " + entry);
						}
					}
					System.out.println();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
	 * @param metric the metric id
	 * @return array of plot data entry if exists, null otherwise.
	 * 
	 * @throws IOException
	 */
	public DataPlotEntry []getPlotEntry(int cct, int metric) throws IOException
	{
		ContextInfo info = listContexts.get(cct);
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
	public DataPlotEntry []getPlotEntry(ContextInfo info, int metric) throws IOException
	{
		if (file == null)
			return null;
		
		if (info == null)
			return null;

		long metricPosition = info.offset + info.numValues * DataPlotEntry.SIZE;
		long size = (info.numNonZeroMetrics + 1) * RECORD_SIZE;
		
		ByteBuffer buffer = file.getChannel().map(MapMode.READ_ONLY, metricPosition, size);
		
		long []indexes = binarySearch((short) metric, 0, info.numNonZeroMetrics+1, buffer);
		//long []indexes = newtonSearch((short) metric, 0, info.numNonZeroMetrics+1, buffer);

		if (indexes == null)
			return null;
		
		file.seek(info.offset +  DataPlotEntry.SIZE * indexes[0]);
		int numMetrics = (int) (indexes[1] - indexes[0]);
		DataPlotEntry []values = new DataPlotEntry[numMetrics];
		
		for (int i=0; i<numMetrics; i++) {
			DataPlotEntry entry = new DataPlotEntry();
			
			entry.metval = file.readDouble();
			entry.tid    = file.readInt();
			
			values[i] = entry;
		}
		
		return values;
	}
	
	
	//////////////////////////////////////////////////////////////////////////
	// Private methods
	//////////////////////////////////////////////////////////////////////////

	private static final int RECORD_SIZE = 2 + 8;
	
	/***
	 * Binary earch the cct index 
	 * 
	 * @param index the cct index
	 * @param first the beginning of the relative index
	 * @param last  the last of the relative index
	 * @param buffer ByteBuffer of the file
	 * @return 2-length array of indexes: the index of the found cct, and its next index
	 */
	private long[] binarySearch(short index, int first, int last, ByteBuffer buffer) {
		int begin = first;
		int end   = last;
		int mid   = (begin+end)/2;
		
		while (begin <= end) {
			buffer.position(mid * RECORD_SIZE);
			
			short metric = buffer.getShort();
			long offset  = buffer.getLong();
			
			if (metric < index) {
				begin = mid+1;
			} else if(metric == index) {
				long nextIndex = offset;
				
				if (mid+1<last) {
					buffer.position(RECORD_SIZE * (mid+1));
					buffer.getShort();
					nextIndex = buffer.getLong();
				}
				return new long[] {offset, nextIndex};
			} else {
				end = mid-1;
			}
			mid = (begin+end)/2;
		}
		// not found
		return null;
	}

	
	/***
	 * Newton-style of Binary search the cct index 
	 * 
	 * @param metric_index the metric index
	 * @param first the beginning of the relative index
	 * @param last  the last of the relative index
	 * @param buffer ByteBuffer of the file
	 * @return 2-length array of indexes: the index of the found cct, and its next index
	 */
	private long[] newtonSearch(int metric_index, int first, int last, ByteBuffer buffer) {
		int left_index  = first;
		int right_index = last - 1;
		
		short left_metric  = getMetric(buffer, left_index);
		short right_metric = getMetric(buffer, right_index);
		
		while (right_index - left_index > 1) {
			
			int predicted_index;
			final float cct_range = right_metric - left_metric;
			final float rate = cct_range / (right_index - left_index);
			final int mid_cct = (int) cct_range / 2;
			
			if (metric_index <= mid_cct) {
				predicted_index = (int) Math.max( ((metric_index - left_metric)/rate) + left_index, left_index);
			} else {
				predicted_index = (int) Math.min(right_index - ((right_metric-metric_index)/rate), right_index);
			}
			
			if (predicted_index <= left_metric) {
				predicted_index = left_index + 1;
			} 
			if (predicted_index >= right_metric) {
				predicted_index = right_index - 1;
			}
			
			short current_metric = getMetric(buffer, predicted_index);
			
			if (metric_index >= current_metric) {
				left_index = predicted_index;
				left_metric = current_metric;
			} else {
				right_index = predicted_index;
				right_metric = current_metric;
			}
		}
		
		boolean found = metric_index == left_metric || metric_index == right_metric;
		
		if (found) {
			int index = left_index;
			if (metric_index == right_metric) 
				// corrupt data: should throw exception 
				index = right_index;
			
			long o1 = getOffset(buffer, index);
			long o2 = getOffset(buffer, index + 1);

			return new long[] {o1, o2};
		}
		// not found: the cct node has no metrics. 
		// we may should return array of zeros instead of null
		return null;
	}

	
	private short getMetric(ByteBuffer buffer, int position) {
		buffer.position(position * RECORD_SIZE);
		return buffer.getShort();
	}
	
	private long getOffset(ByteBuffer buffer, int position) {
		buffer.position(position * RECORD_SIZE + Short.SIZE);
		return buffer.getLong();
	}
	
	
	//////////////////////////////////////////////////////////////////////////
	// Private classes
	//////////////////////////////////////////////////////////////////////////

	private static class ContextInfo
	{
		public static final int SIZE = 4 + 8 + 2 + 8;
		
		public int   id;
		public long  numValues;
		public short numNonZeroMetrics;
		public long  offset;
		
		public String toString() {
			return  "id: "   + id + 
					", nv: " + numValues +
					", nm: " + numNonZeroMetrics +
					", of: " + offset;
		}
	}

	
	
	/////////////////////////////////////////////////////////////////////////////
	// unit test
	/////////////////////////////////////////////////////////////////////////////
	static public void main(String []argv)
	{
		final DataPlot data = new DataPlot();
		String filename;
		if (argv != null && argv.length>0) {
			filename = argv[0];
		} else {
			filename = "/Users/la5/data/sparse/gpu/hpctoolkit-fib-sparse-database/cct.db"; 
		}
		try {
			data.open(filename);
			data.printInfo(System.out);
			data.dispose();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
