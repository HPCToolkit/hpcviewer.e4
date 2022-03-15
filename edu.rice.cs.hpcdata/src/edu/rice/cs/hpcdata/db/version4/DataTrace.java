package edu.rice.cs.hpcdata.db.version4;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Random;

/*******************************************************************************
 * 
 * Class to read data trace from file (via DataCommon) and store the info
 * 
 * User needs to use open() method to start opening the file
 *
 *******************************************************************************/
public class DataTrace extends DataCommon 
{
	public final static  String FILE_TRACE_DB = "trace.db";
	
	private final static String HEADER = "HPCTOOLKITtrce";
	private final static int FMT_TRACEDB_SZ_CTX_SAMPLE = 0x0c;
	private static final int NUM_ITEMS = 1;

	private TraceHeader  traceHeader;
	private TraceContext []traceCtxs;
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.db.DataCommon#open(java.lang.String)
	 */
	public void open(final String file)
			throws IOException
	{
		super.open(file + File.separator + FILE_TRACE_DB);
	}
	

	@Override
	protected int getNumSections() {
		return NUM_ITEMS;
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
		// -------------------------------------------------
		// reading the next 256 byte header
		// -------------------------------------------------
		ByteBuffer buffer = input.map(MapMode.READ_ONLY, sections[0].offset, sections[0].size);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		traceHeader = new TraceHeader(buffer);
		
		long size = (long)traceHeader.nTraces * traceHeader.szTrace;
		buffer = input.map(MapMode.READ_ONLY, traceHeader.pTraces, size);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		traceCtxs = new TraceContext[traceHeader.nTraces];
		
		for(int i=0; i<traceHeader.nTraces; i++) {
			int profIndex = buffer.getInt();
			var tc = new TraceContext(buffer);
			traceCtxs[i] = tc;
		}

		return true;
	}

	
	public long getMinTime() {
		return traceHeader.minTimestamp;
	}
	
	
	public long getMaxTime() {
		return traceHeader.maxTimestamp;
	}
	
	/****
	 * get a trace record from a given rank and sample index
	 * 
	 * @param rank 
	 * 			the sequence profile number starts from number 0
	 * @param index
	 * 			sample index
	 * 
	 * @return {@code DataRecord} containing time and cct ID
	 * 
	 * @throws IOException
	 */
	public DataRecord getSampledData(int rank, long index) throws IOException
	{
		assert(rank < traceCtxs.length);
		
		var tc = traceCtxs[rank];		
		if (tc == null)
			return null;
		
		long position = tc.pStart + (index * FMT_TRACEDB_SZ_CTX_SAMPLE);
		var buffer = getChannel().map(MapMode.READ_ONLY, position, FMT_TRACEDB_SZ_CTX_SAMPLE);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		long time = buffer.getLong();
		int  cpid = buffer.getInt();
		DataRecord data = new DataRecord(time, cpid, 0);
		
		return data;
	}
	
	/***
	 * Retrieve the number of samples of a given ranks
	 * 
	 * @param rank
	 * 			the sequence profile number starts from number 0
	 * @return int 
	 * 			the number of samples
	 */
	public int getNumberOfSamples(int rank)
	{
		assert(rank < traceCtxs.length);
		var tc = traceCtxs[rank];		
		if (tc == null)
			return 0;
		
		long numBytes = tc.pEnd - tc.pStart;
		return (int) (numBytes / FMT_TRACEDB_SZ_CTX_SAMPLE);
	}
	
	/****
	 * Retrieve the number of ranks (either processes or threads or both)
	 * in the current data traces
	 * 
	 * @return
	 */
	public int getNumberOfRanks()
	{
		return (int) traceCtxs.length;
	}
	
	
	/****
	 * 
	 * @param rank
	 * 			the sequence profile number starts from number 0
	 * @return long
	 * 			the number of bytes
	 */
	public long getLength(int rank)
	{
		assert(rank < traceCtxs.length);
		
		var tc = traceCtxs[rank];		
		if (tc == null)
			throw new RuntimeException("Invalid rank: " + rank);
		
		return tc.pEnd - tc.pStart;
	}
	
	
	public long getOffset(int rank)
	{
		assert(rank < traceCtxs.length);
		var tc = traceCtxs[rank];		
		if (tc == null)
			throw new RuntimeException("Invalid rank: " + rank);
		return tc.pStart;
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
			int rank = 1 + (nranks>1 ? r.nextInt(nranks-1) : 0);
			int numsamples = getNumberOfSamples(rank);
			int sample = r.nextInt(numsamples);
			try {
				out.format("%d:  %s\n", rank, getSampledData(rank, sample));
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
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
		var buffer = getChannel().map(MapMode.READ_ONLY, position, 8);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer.getLong();
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
		var buffer = getChannel().map(MapMode.READ_ONLY, position, 4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer.getInt();
	}
	
	@Deprecated
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
		var buffer = getChannel().map(MapMode.READ_ONLY, position, 4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer.getDouble();
	}
	
	// --------------------------------------------------------------------
	// Private methods
	// --------------------------------------------------------------------
	

	
	/******************
	 * 
	 * Context trace record containing the start and end of a profile trace
	 *
	 ******************/
	private static class TraceContext
	{
		public final long pStart;
		public final long pEnd;
		
		public TraceContext(ByteBuffer buffer) {
			pStart = buffer.getLong(0x08);
			pEnd   = buffer.getLong(0x10);
		}
	}

	/****
	 * 
	 * Class to store the header of trace database
	 *
	 */
	private static class TraceHeader
	{
		public final long pTraces;
		public final int  nTraces;
		public final byte szTrace;
		public final long minTimestamp;
		public final long maxTimestamp;
		
		public TraceHeader(ByteBuffer buffer) {
			pTraces = buffer.getLong();
			nTraces = buffer.getInt();
			szTrace = buffer.get();
			
			minTimestamp = buffer.getLong(0x10);
			maxTimestamp = buffer.getLong(0x18);
		}
		
		public String toString() {
			return String.format("0x%x %d - %d", pTraces, szTrace, nTraces);
		}
	}
}
