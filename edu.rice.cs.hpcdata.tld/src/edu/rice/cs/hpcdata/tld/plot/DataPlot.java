package edu.rice.cs.hpcdata.tld.plot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;
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
	final private static int PLOT_ENTRY_SIZE = Constants.SIZEOF_INT + Constants.SIZEOF_INT;
	private long index_start;
	private long index_length;
	private long plot_start;
	private long plot_length;
	private int  size_cctid;
	private int  size_metid;
	private int  size_offset;
	private int  size_count;
	private int  size_tid;
	private int  size_metval;
	
	private RandomAccessFile file;
	
	private HashMap<PlotIndexKey, PlotIndexValue> table_index;
	
	
	//////////////////////////////////////////////////////////////////////////
	// Override methods from DataCommon
	//////////////////////////////////////////////////////////////////////////

	
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
		return true;
	}

	@Override
	protected boolean readNextHeader(FileChannel input) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(256);
		int numBytes      = input.read(buffer);
		if (numBytes > 0) 
		{
			buffer.flip();
			index_start  = buffer.getLong();
			index_length = buffer.getLong();
			plot_start	 = buffer.getLong();
			plot_length	 = buffer.getLong();
			
			size_cctid 	 = buffer.getInt();
			size_metid 	 = buffer.getInt();
			size_offset  = buffer.getInt();
			size_count 	 = buffer.getInt();
			size_tid 	 = buffer.getInt();
			size_metval  = buffer.getInt();
		}
		return true;
	}

	@Override
	public void printInfo( PrintStream out)
	{
		super.printInfo(out);
		out.format("index start: %d\n index length: %d\n plot start: %d\n plot length: %d\n", 
				index_start, index_length, plot_start, plot_length);
		out.format("\n size cct id: %d\n size met id: %d\n size offset: %d\n size  count: %d\n", 
				size_cctid, size_metid, size_offset, size_count);
		out.format(" size tid: %d\n size met val: %d\n", size_tid, size_metval);
		
		try {
			checkData();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		
		// reading some parts of the indexes
		Random r = new Random();
		for(int i=0; i<10; i++)
		{
			int index  = r.nextInt(table_index.size() - 1);
			int metric = r.nextInt(2);
			PlotIndexValue pi = table_index.get(new PlotIndexKey(index, metric));
			if (pi != null)
			{
				out.format("[%d]\t met-id:%d, offset: %d, count: %d\n", 
						index, metric, pi.offset, pi.count);
				try {
					DataPlotEntry []entry = getPlotEntry(index, 0);
					if (entry != null) 
					{
						for (int j=0; j<pi.count; j++)
						{
							out.format("\t%s", entry[j]);
						}
						out.println();
					}
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
		checkData();
		
		PlotIndexValue pi = table_index.get(new PlotIndexKey(cct, metric));
		if (pi == null)
			// there is no data for the given cct and metric
			return null;
		file.seek(pi.offset);
		byte []buffer = new byte[PLOT_ENTRY_SIZE * pi.count];
		file.readFully(buffer);
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
		DataPlotEntry []entry = new DataPlotEntry[pi.count];
		for (int i=0; i<pi.count; i++)
		{
			
			entry[i] 	    = new DataPlotEntry();
			entry[i].tid 	= byteBuffer.getInt();
			entry[i].metval = byteBuffer.getFloat();
		}
		return entry;
	}
	
	
	//////////////////////////////////////////////////////////////////////////
	// Private methods
	//////////////////////////////////////////////////////////////////////////

	private RandomAccessFile internal_open(String filename) throws FileNotFoundException
	{
		RandomAccessFile file = new RandomAccessFile(filename, "r");
		return file;

	}
	
	private void checkData() throws IOException
	{
		if (table_index == null) 
		{
			fillOffsetTable(filename);
			file = internal_open(filename);
		}
	}
	
	private void fillOffsetTable(String filename) throws IOException
	{
		final RandomAccessFile file = new RandomAccessFile(filename, "r");
		final FileChannel channel = file.getChannel();
		final MappedByteBuffer mappedBuffer = channel.map(MapMode.READ_ONLY, index_start, index_length);
		final ByteBuffer byteBuffer = mappedBuffer.asReadOnlyBuffer();
		
		final int INDEX_PLOT_SIZE = Constants.SIZEOF_INT  + Constants.SIZEOF_INT +
								 	Constants.SIZEOF_LONG + Constants.SIZEOF_LONG; 
		final int num_index = (int) (index_length/INDEX_PLOT_SIZE);
		
		table_index  = new HashMap<PlotIndexKey, PlotIndexValue>(num_index); 
		
		for (int i=0; i<num_index; i++)
		{
			int cct_id 	     = byteBuffer.getInt();
			int metric_id    = byteBuffer.getInt();
			PlotIndexKey idx = new PlotIndexKey(cct_id, metric_id);
			
			PlotIndexValue val = new PlotIndexValue();
			val.offset 	  = byteBuffer.getLong();
			val.count	  = (int) byteBuffer.getLong();
			
			table_index.put(idx, val);
		}
		channel.close();
		file.close();
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
			filename = "/home/la5/data/new-database/db-lulesh-new/plot.db"; 
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
