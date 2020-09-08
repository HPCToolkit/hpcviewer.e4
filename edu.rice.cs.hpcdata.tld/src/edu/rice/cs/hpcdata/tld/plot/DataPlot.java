package edu.rice.cs.hpcdata.tld.plot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import edu.rice.cs.hpc.data.db.DataCommon;
import edu.rice.cs.hpc.data.util.Constants;

/*******************************************************************************************
 * 
 * Class to manage plot.db file
 * <p>See {@link getPlotEntry} to get the list of plot data</p>
 *
 *******************************************************************************************/
public class DataPlot extends DataCommon 
{
	private static final String HEADER = "HPCPROF-cmsdb_____";
	
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
	protected boolean readNextHeader(FileChannel input) throws IOException {
		if (numItems == 0)
			return false;
		
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
		return true;
	}

	@Override
	public void printInfo( PrintStream out)
	{
		super.printInfo(out);
		
		// reading some parts of the indexes
		Random r = new Random();
		for(int i=0; i<10; i++)
		{
		}
	}
	
	
	//////////////////////////////////////////////////////////////////////////
	// public methods
	//////////////////////////////////////////////////////////////////////////

	/******
	 * Retrieve a list of plot data of a given cct index and metric raw index
	 *  
	 * @param cct : cct index
	 * @param metric : the index of the raw metric
	 * 
	 * @return an array of plot data
	 * 
	 * @throws IOException
	 */
	public DataPlotEntry []getPlotEntry(int cct, int metric) throws IOException
	{
		if (file == null)
			return null;
		
		int index = (cct-1) / 2;
		ContextInfo info = listContexts.get(index);
		if (info == null)
			return null;

		long metricPosition = info.offset + info.numValues * 12;
		long size = info.numNonZeroMetrics + 1;
		ByteBuffer buffer = file.getChannel().map(MapMode.READ_ONLY, metricPosition, size);
		long []indexes = binarySearch((short) metric, 0, info.numNonZeroMetrics, buffer);

		if (indexes == null)
			return null;
		
		file.seek(indexes[0]);
		int numMetrics = (int) (indexes[1] - indexes[0]);
		DataPlotEntry []entries = new DataPlotEntry[numMetrics];
		
		for (int i=0; i<numMetrics; i++) {
			DataPlotEntry entry = new DataPlotEntry();
			entry.metval = file.readFloat();
			entry.tid    = file.read();
			
			entries[i] = entry;
		}
		
		return entries;
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

	private static class PlotIndexKey
	{
		int cct_index, metric_id;
		
		public PlotIndexKey(int cct_index, int metric_id)
		{
			this.cct_index = cct_index;
			this.metric_id = metric_id;
		}
		
		@Override
		public int hashCode()
		{
			return (metric_id << 24 + cct_index);
		}
		
		@Override
		public boolean equals(Object o) 
		{
			if (this == o) return true;
			if (!(o instanceof PlotIndexKey)) return false;
			PlotIndexKey key = (PlotIndexKey) o;
			return cct_index == key.cct_index && metric_id == key.metric_id;
		}
	}
	private static class PlotIndexValue
	{
		public long offset;
		public int count;
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
			filename = "/Users/la5/data/sparse/hpctoolkit-fib-database/cct.db"; 
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
