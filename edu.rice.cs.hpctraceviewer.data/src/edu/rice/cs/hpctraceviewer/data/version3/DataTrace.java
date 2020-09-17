package edu.rice.cs.hpctraceviewer.data.version3;

import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Random;

import edu.rice.cs.hpc.data.db.version3.DataCommon;
import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpc.data.util.LargeByteBuffer;
import edu.rice.cs.hpctraceviewer.data.DataRecord;

/*******************************************************************************
 * 
 * Class to read data trace from file (via DataCommon) and store the info
 * 
 * User needs to use open() method to start opening the file
 *
 *******************************************************************************/
public class DataTrace extends DataCommon 
{
	final static private String TRACE_NAME = "hpctoolkit trace metrics";
	final static private int RECORD_INDEX_SIZE = Constants.SIZEOF_LONG + 
										Constants.SIZEOF_LONG + Constants.SIZEOF_LONG;
	final static public int RECORD_ENTRY_SIZE = Constants.SIZEOF_LONG + Constants.SIZEOF_INT;
	final static private int TRACE_HEADER_SIZE = 256;
	
	long index_start, index_length;
	long trace_start, trace_length;
	long min_time,	  max_time;

	int  size_offset, size_length;
	int  size_gtid,	  size_time;
	int  size_cctid;
	
	private RandomAccessFile file;
	private FileChannel channel;
	private LargeByteBuffer lbBuffer;

	private long []table_offset;
	private long []table_length;
	private long []table_global_tid;
	


	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.db.DataCommon#open(java.lang.String)
	 */
	public void open(final String file)
			throws IOException
	{
		super.open(file);
		
		open_internal(file);
		// fill the cct offset table
		fillOffsetTable(file);
	}
	
	/***
	 * Return the lowest begin time of all ranks
	 * 
	 * @return the minimum time
	 */
	public long getMinTime()
	{
		return min_time;
	}

	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.db.DataCommon#isTypeFormatCorrect(long)
	 */
	protected boolean isTypeFormatCorrect(long type) {
		return type == 2;
	}

	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.db.DataCommon#isFileHeaderCorrect(java.lang.String)
	 */
	protected boolean isFileHeaderCorrect(String header) {
		return header.compareTo(TRACE_NAME) >= 0;
	}

	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.db.DataCommon#readNextHeader(java.nio.channels.FileChannel)
	 */
	protected boolean readNextHeader(FileChannel input)
			throws IOException
	{
		// -------------------------------------------------
		// reading the next 256 byte header
		// -------------------------------------------------
		ByteBuffer buffer = ByteBuffer.allocate(TRACE_HEADER_SIZE);
		int numBytes      = input.read(buffer);
		if (numBytes > 0) 
		{
			buffer.flip();
			
			index_start  = buffer.getLong();
			index_length = buffer.getLong();
			
			trace_start  = buffer.getLong();
			trace_length = buffer.getLong();
			
			min_time = buffer.getLong();
			max_time = buffer.getLong();
			
			size_offset = buffer.getInt();
			size_length = buffer.getInt();
			size_gtid   = buffer.getInt();
			size_time   = buffer.getInt();
			size_cctid  = buffer.getInt();
			
			// FIXME: At the moment we cannot afford if the size of cct is not integer
			if (size_cctid != 4)
			{
				throw new IOException("The size of CCT is not supported: " + size_cctid);
			} 
		}
		
		return true;
	}

	/****
	 * get a trace record from a given rank and sample index
	 * 
	 * @param rank : the rank 
	 * @param index : sample index
	 * 
	 * @return DataRecord containing time and cct ID
	 * 
	 * @throws IOException
	 */
	public DataRecord getSampledData(int rank, long index) throws IOException
	{
		if (table_offset[rank] <=0)
			return null;
		
		long file_size = file.length();
		long offset = table_offset[rank] + (index * RECORD_ENTRY_SIZE);
		
		if (file_size > offset)
		{
			file.seek(offset);
			byte []buffer_byte = new byte[RECORD_ENTRY_SIZE];
			file.readFully(buffer_byte);
			ByteBuffer buffer  = ByteBuffer.wrap(buffer_byte);
			
			long time = buffer.getLong();
			int cct   = buffer.getInt();
			return new DataRecord(time, cct, 0);
		}
		return null;
	}
	
	/***
	 * Retrieve the number of samples of a given ranks
	 * 
	 * @param rank
	 * @return int the number of samples
	 */
	public int getNumberOfSamples(int rank)
	{
		return (int) (table_length[rank] / RECORD_ENTRY_SIZE);
	}
	
	/****
	 * Retrieve the number of ranks (either processes or threads or both)
	 * in the current data traces
	 * 
	 * @return
	 */
	public int getNumberOfRanks()
	{
		return (int) numItems;
	}
	
	public long []getOffsets()
	{
		return table_offset;
	}
	
	public long getLength(int rank)
	{
		return table_length[rank];
	}
	
	public long getOffset(int rank)
	{
		return table_offset[rank];
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.db.DataCommon#printInfo(java.io.PrintStream)
	 */
	public void printInfo( PrintStream out)
	{
		super.printInfo(out);
		out.println("Min time: " + min_time);
		out.println("Max time: " + max_time);
		
		out.println("index start: " + index_start);
		out.println("index length: " + index_length);
		
		out.println(" trace start: " + trace_start + "\n trace length: " + trace_length);
		
		out.println("size offset: " + size_offset);
		out.println("size length: " + size_length);
		out.println("size time: " + size_time);
		out.println("size cctid: " + size_cctid + "\n size gtid: " + size_gtid);
		
		// print the table index
		for(int i=0; i<table_offset.length; i++)
		{
			out.format(" %d. %05x : %04x\n", table_global_tid[i], table_offset[i], table_length[i]);
		}
		Random r = new Random();
		int nranks = getNumberOfRanks();
		
		for(int i=0; i< 10; i++)
		{
			int rank = (nranks>1 ? r.nextInt(nranks-1) : 0);
			int numsamples = getNumberOfSamples(rank);
			int sample = r.nextInt(numsamples);
			try {
				out.format("%d:  %s\n", rank, getSampledData(rank, sample));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.db.DataCommon#dispose()
	 */
	public void dispose() throws IOException
	{
		channel.close();
		file.close();
		lbBuffer.dispose();
	}
	// --------------------------------------------------------------------
	// For the sake of compatibility, we need to provide these methods
	// --------------------------------------------------------------------
	/****
	 * @deprecated method to get a 8 byte long from a given file absolute location
	 * This method is to be replaced with {@link getSampledData}
	 * 
	 * <p> At the moment, the implementation of this method requires a mutual exclusive
	 * to the file (synchronized). It is then important this method to be executed 
	 * as fast as possible and let the caller to handle the execution and all
	 * checking and verification.</p>
	 * 
	 * @param position : absolute location of the file
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public long getLong(long position) throws IOException
	{
		return lbBuffer.getLong(position);
	}
	
	@Deprecated
	/*****
	 * method to get a 4 byte integer from an absolute position
	 * This method is to be replaced with {@link getSampledData}
	 * 
	 * <p> At the moment, the implementation of this method requires a mutual exclusive
	 * to the file (synchronized). It is then important this method to be executed 
	 * as fast as possible and let the caller to handle the execution and all
	 * checking and verification.</p>
	 *  
	 * @param position
	 * @return
	 * @throws IOException
	 */
	public int getInt(long position) throws IOException
	{
		return lbBuffer.getInt(position);
	}
	
	/**
	 * @deprecated method to get a 8 byte double from an absolute position
	 * This method is to be replaced with {@link getSampledData}
	 * 
	 * <p> At the moment, the implementation of this method requires a mutual exclusive
	 * to the file (synchronized). It is then important this method to be executed 
	 * as fast as possible and let the caller to handle the execution and all
	 * checking and verification.</p>
	 * 
	 * @param position
	 * @return
	 * @throws IOException
	 */
	public double getDouble(long position) throws IOException
	{
		return lbBuffer.getDouble(position);
	}
	
	// --------------------------------------------------------------------
	// Private methods
	// --------------------------------------------------------------------
	
	private void open_internal(String filename) throws IOException
	{
		file 	= new RandomAccessFile(filename, "r");
		channel = file.getChannel();
		lbBuffer = new LargeByteBuffer(channel, RECORD_ENTRY_SIZE, RECORD_ENTRY_SIZE);
	}


	private void fillOffsetTable(final String filename)
			throws IOException
	{
		// map all the table into memory. 
		// This statement can be problematic if the offset_size is huge
		
		MappedByteBuffer mappedBuffer = channel.map(MapMode.READ_ONLY, index_start, index_length);
		LongBuffer longBuffer = mappedBuffer.asLongBuffer();
		
		final int num_pos = (int) (index_length /  RECORD_INDEX_SIZE);
		
		table_offset 	 = new long[(int) num_pos];
		table_length 	 = new long[(int) num_pos];
		table_global_tid = new long[(int) num_pos];
		
		for (int i=0; i<num_pos; i++)
		{
			int index		    = 3 * i; 
			table_offset[i] 	= longBuffer.get(index);
			table_length[i] 	= longBuffer.get(index+1);
			table_global_tid[i] = longBuffer.get(index+2);
		}
	}
	

	/***************************
	 * unit test 
	 * 
	 * @param argv
	 ***************************/
	public static void main(String []argv)
	{
		final DataTrace trace_data = new DataTrace();
		final String filename;
		if (argv != null && argv.length>0) 
		{
			filename = argv[0];
		} else {
			filename = "/home/la5/data/new-database/db-lulesh-new/trace.db";
		}
		try {
			trace_data.open(filename);			
			trace_data.printInfo(System.out);
			trace_data.dispose();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
