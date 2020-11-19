package edu.rice.cs.hpc.data.db.version3;

import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Random;

import edu.rice.cs.hpc.data.util.LargeByteBuffer;

/*******************************************************************************
 * 
 * Class to read data trace from file (via DataCommon) and store the info
 * 
 * User needs to use open() method to start opening the file
 *
 *******************************************************************************/
public class DataTrace extends DataCommon 
{
	private final static String HEADER = "HPCPROF-tracedb_";
	private final static int TRACE_HDR_RECORD_SIZE = 22;
	private final static int TRACE_RECORD_SIZE = 8 + 4;
	
	private RandomAccessFile file;
	private FileChannel channel;
	private LargeByteBuffer lbBuffer;

	private AbstractMap<Integer, TraceHeader> mapProfToTrace;

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
		return header.equals(HEADER);
	}

	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.db.DataCommon#readNextHeader(java.nio.channels.FileChannel)
	 */
	protected boolean readNextHeader(FileChannel input, DataSection []sections)
			throws IOException
	{
		input.position(sections[0].offset);
		
		// -------------------------------------------------
		// reading the next 256 byte header
		// -------------------------------------------------
		long trace_hdr_size = TRACE_HDR_RECORD_SIZE * numItems;
		ByteBuffer buffer = ByteBuffer.allocate((int) trace_hdr_size);
		
		int numBytes      = input.read(buffer);
		if (numBytes <= 0) 
			return false;
		
		buffer.flip();
		
		mapProfToTrace = new HashMap<Integer, DataTrace.TraceHeader>((int) numItems);
		
		for(int i=0; i<numItems; i++) {
			TraceHeader header = new TraceHeader();
			
			// this database starts the profile number with number 1
			// the old database starts with number 0
			
			header.profIndex  = buffer.getInt();
			header.traceIndex = buffer.getShort();
			
			header.start = buffer.getLong();
			header.end   = buffer.getLong();
			
			mapProfToTrace.put(header.profIndex, header);
		}
		buffer.clear();
		
		long nextPosition = sections[0].offset + getMultiplyOf8( sections[0].size);
		input.position(nextPosition);

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
		if (rank == 0)
			System.out.println();
		
		int profileNum = rank;
		TraceHeader th = mapProfToTrace.get(profileNum);
		if (th == null)
			return null;
		
		long position = th.start + TRACE_RECORD_SIZE * index;
		long time = lbBuffer.getLong(position);
		int  cpid = lbBuffer.getInt(position + 8);
		DataRecord data = new DataRecord(time, cpid, 0);
		
		return data;
	}
	
	/***
	 * Retrieve the number of samples of a given ranks
	 * 
	 * @param rank
	 * @return int the number of samples
	 */
	public int getNumberOfSamples(int rank)
	{
		TraceHeader th = mapProfToTrace.get(rank);
		if (th == null)
			return 0;
		
		long numBytes = th.end - th.start;
		return (int) (numBytes / TRACE_RECORD_SIZE);
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
	
	
	public long getLength(int rank)
	{
		TraceHeader th = mapProfToTrace.get(rank);
		if (th != null) {
			return th.end - th.start - TRACE_RECORD_SIZE;
		}
		throw new RuntimeException("Invalid rank: " + rank);
	}
	
	
	public long getOffset(int rank)
	{
		TraceHeader th = mapProfToTrace.get(rank);
		if (th != null) {
			return th.start;
		}
		throw new RuntimeException("Invalid rank: " + rank);
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.db.DataCommon#printInfo(java.io.PrintStream)
	 */
	public void printInfo( PrintStream out)
	{

		Random r = new Random();
		int nranks = getNumberOfRanks();
		
		for(int i=0; i< 10; i++)
		{
			int rank = (nranks>1 ? r.nextInt(nranks-1) : 1);
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
		lbBuffer = new LargeByteBuffer(channel, 2, TRACE_RECORD_SIZE);
	}


	/****
	 * 
	 * Class to store the header of trace database
	 *
	 */
	static class TraceHeader
	{
		int   profIndex;   // profile number 		
		short traceIndex;  // style: 0-> cct-style, 1->metric-style
		long start;		   // start of the offset of this profile
		long end;		   // end of the offset for this profile
		
		public String toString() {
			return profIndex + ": " + start +"-" + end;
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
			filename = "/Users/la5/data/sparse/gpu/hpctoolkit-tower-one-sparse-database/trace.db";
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
