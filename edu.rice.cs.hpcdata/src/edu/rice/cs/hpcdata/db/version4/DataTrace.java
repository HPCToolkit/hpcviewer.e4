package edu.rice.cs.hpcdata.db.version4;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;


import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpcdata.util.LargeByteBuffer;

/*******************************************************************************
 * 
 * Class to read data trace from file (via DataCommon) and store the info
 * 
 * User needs to use open() method to start opening the file
 *
 *******************************************************************************/
public class DataTrace extends DataCommon 
{
	private static final String HEADER = "HPCTOOLKITtrce";
	private static final int FMT_TRACEDB_SZ_CTX_SAMPLE = 0x0c;
	private static final int NUM_ITEMS = 1;

	private TraceHeader  traceHeader;
	private TraceContext[] traceCtxs;
	private LargeByteBuffer lbBuffer;
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.db.DataCommon#open(java.lang.String)
	 */
	public void open(final String file)
			throws IOException
	{
		super.open(file + File.separator + Constants.TRACE_FILE_SPARSE_VERSION);
	}
	

	@Override
	public void dispose() throws IOException
	{
		if (lbBuffer != null)
			lbBuffer.dispose();
		super.dispose();
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
	protected boolean isFileFooterCorrect(String header) {
		return header.equals("trace.db");
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
			int loc = i * traceHeader.szTrace;			
			traceCtxs[i] = new TraceContext(buffer, loc);
		}
		
		lbBuffer = new LargeByteBuffer(input, 2, FMT_TRACEDB_SZ_CTX_SAMPLE);
		lbBuffer.setEndian(ByteOrder.LITTLE_ENDIAN);

		return true;
	}

	
	public int getRecordSize() {
		return traceHeader.szTrace;
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
		var tc = traceCtxs[rank];		
		if (tc == null)
			return null;
		
		long position = tc.pStart + (index * FMT_TRACEDB_SZ_CTX_SAMPLE);
		
		long time = lbBuffer.getLong(position);
		int  cpid = lbBuffer.getInt(position + 8);
		return new DataRecord(time, cpid, 0);
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
		return traceCtxs.length;
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
		var tc = traceCtxs[rank];		
		if (tc == null)
			throw new RuntimeException("Invalid rank: " + rank);
		
		return tc.pEnd - tc.pStart;
	}
	
	
	public long getOffset(int rank)
	{
		var tc = traceCtxs[rank];		
		if (tc == null)
			throw new RuntimeException("Invalid rank: " + rank);
		return tc.pStart;
	}
	
	public long getEndOffset(int rank)
	{
		var tc = traceCtxs[rank];		
		if (tc == null)
			throw new RuntimeException("Invalid rank: " + rank);
		return tc.pEnd;
	}

	
	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.db.DataCommon#printInfo(java.io.PrintStream)
	 */
	public void printInfo( PrintStream out)
	{
		int nranks = getNumberOfRanks();
		
		for(int rank=0; rank< nranks; rank++)
		{
			int numsamples = getNumberOfSamples(rank);
			for(int index=0; index<numsamples; index++) {
				try {
					out.format("%d:  %s%n", rank, getSampledData(rank, index));
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
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
		return lbBuffer.getDouble(position);
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
		final int profIndex;
		final long pStart;
		final long pEnd;
		
		/***
		 * Record of the start and end of a trace profile
		 * @param buffer 
		 * 			The file buffer
		 * @param loc
		 * 			The beginning of the location of the trace profile
		 */
		public TraceContext(ByteBuffer buffer, int loc) {
			// 00: 	u32 	profIndex 	4.0 	Index of a profile listed in the profile.db			
			// 08: 	{Elem}* 	pStart 	4.0 	Pointer to the first element of the trace line (array)
			// 10: 	{Elem}* 	pEnd 	4.0 	Pointer to the after-end element of the trace line (array)
			
			profIndex = buffer.getInt(loc);
			pStart = buffer.getLong(loc + 0x08);
			pEnd   = buffer.getLong(loc + 0x10);
		}
		
		public String toString() {
			return String.format("%d: 0x%x - 0x%x", profIndex, pStart, pEnd);
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
			// 00: 	{CTH}[nTraces]* 	pTraces 	4.0 	Header for each trace
			// 08: 	u32 	nTraces 	4.0 	Number of traces listed in this section
			// 0c: 	u8 		szTrace 	4.0 	Size of a {TH} structure, currently 24
			//				
			// 10: 	u64 	minTimestamp 	4.0 	Smallest timestamp of the traces listed in *pTraces
			// 18: 	u64 	maxTimestamp 	4.0 	Largest timestamp of the traces listed in *pTraces
			
			pTraces = buffer.getLong();
			nTraces = buffer.getInt(0x08);
			szTrace = buffer.get(0x0c);
			
			minTimestamp = buffer.getLong(0x10);
			maxTimestamp = buffer.getLong(0x18);
		}
		
		public String toString() {
			return String.format("0x%x %d - %d", pTraces, szTrace, nTraces);
		}
	}
}
