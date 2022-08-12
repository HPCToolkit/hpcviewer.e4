package edu.rice.cs.hpcdata.db.version2;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import edu.rice.cs.hpcdata.db.IFileDB;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpcdata.util.LargeByteBuffer;
import edu.rice.cs.hpcdata.util.Util;

/***************************************
 * 
 * An implementation of IFileDB for format 2.0
 *
 ***************************************/
public class FileDB2 implements IFileDB 
{
	//-----------------------------------------------------------
	// Global variables
	//-----------------------------------------------------------
	
	private int type = Constants.MULTI_PROCESSES | Constants.MULTI_THREADING; // default is hybrid
	
	private LargeByteBuffer masterBuff;
	
	private int numFiles = 0;
	private String[] valuesX;
	private long[] offsets;
	
	private int recordSz;
	private int headerSize;
	
	private RandomAccessFile file; 
	
	private List<IdTuple> listIdTuples;
	private IdTupleType   listIdTupleTypes;

	@Override
	public void open(String filename, int headerSize, int recordSz)  throws IOException 
	{
		if (filename != null) {
			// read header file
			readHeader(filename, headerSize, recordSz);
		}
	}
	
	@Override
	public String []getRankLabels() {
		return valuesX;
	}
	
	@Override
	public int getNumberOfRanks() 
	{
		return this.numFiles;
	}
	
	@Override
	public long[] getOffsets() 
	{
		return this.offsets;
	}
	
	@Override
	public List<IdTuple> getIdTuple(IdTupleOption option) {
		return listIdTuples;
	}
	
	
	/***
	 * Read the header of the file and get info needed for further actions
	 * 
	 * @param f: array of files
	 * @throws IOException 
	 */
	private void readHeader(String filename, int headerSize, int recordSz)
			throws IOException {
		
		this.recordSz   = recordSz; 
		this.headerSize = headerSize;
		
		file = new RandomAccessFile(filename, "r");
		final FileChannel f = file.getChannel();
		masterBuff = new LargeByteBuffer(f, headerSize, recordSz);

		this.type = masterBuff.getInt(0);
		this.numFiles = masterBuff.getInt(Constants.SIZEOF_INT);
		
		valuesX = new String[numFiles];
		offsets = new long[numFiles];
		
		listIdTuples = new ArrayList<>(numFiles);
		
		long currentPos = Constants.SIZEOF_INT * 2L;
		int parallelism = getParallelismLevel();

		// get the procs and threads IDs
		for(int i=0; i<numFiles; i++) {

			final int proc_id = masterBuff.getInt(currentPos);
			currentPos += Constants.SIZEOF_INT;
			final int thread_id = masterBuff.getInt(currentPos);
			currentPos += Constants.SIZEOF_INT;
			
			offsets[i] = masterBuff.getLong(currentPos);
			currentPos += Constants.SIZEOF_LONG;
			
			IdTuple tuple = new IdTuple(i+1, parallelism);
			
			//--------------------------------------------------------------------
			// adding list of x-axis 
			//--------------------------------------------------------------------			
			
			String xVal;
			if (this.isHybrid()) 
			{
				xVal = String.valueOf(proc_id) + "." + thread_id;

				tuple.setKind(0, IdTupleType.KIND_RANK);
				tuple.setPhysicalIndex(0, proc_id);
				tuple.setLogicalIndex(0, proc_id);
				
				tuple.setKind(1, IdTupleType.KIND_THREAD);
				tuple.setPhysicalIndex(1, thread_id) ;
				tuple.setLogicalIndex(1, thread_id);
				
			} else if (isMultiProcess()) 
			{
				xVal = String.valueOf(proc_id);					
				
				tuple.setKind(0, IdTupleType.KIND_RANK);
				tuple.setPhysicalIndex(0, proc_id);
				tuple.setLogicalIndex(0, proc_id);
			} else if (isMultiThreading()) 
			{
				xVal = String.valueOf(thread_id);
				
				tuple.setKind(0, IdTupleType.KIND_THREAD);
				tuple.setPhysicalIndex(0, thread_id);
				tuple.setLogicalIndex(0, thread_id);
			} else {
				// temporary fix: if the application is neither hybrid nor multiproc nor multithreads,
				// we just print whatever the order of file name alphabetically
				// this is not the ideal solution, but we cannot trust the value of proc_id and thread_id
				xVal = String.valueOf(i);
				
				// fix issue #222: use thread instead of rank
				// the physical id will be whatever in thread_id, but the logical id will be the file order
				tuple.setKind(0, IdTupleType.KIND_THREAD);
				tuple.setPhysicalIndex(0, thread_id);
				tuple.setLogicalIndex(0, i);
			}
			valuesX[i] = xVal;
			listIdTuples.add(tuple);
		}
	}

	@Override
	public int 	getParallelismLevel()
	{
		// fix issue #222: allow viewing multiple same threads 
		// it should display thread 0, thread 0, ... instead of empty 
		return Math.max(1, Util.countSetBits(type));
	}

	/**
	 * Check if the application is a multi-processing program (like MPI)
	 * 
	 * @return true if this is the case
	 */
	public boolean isMultiProcess() {
		return (type & Constants.MULTI_PROCESSES) != 0;
	}
	
	/**
	 * Check if the application is a multi-threading program (OpenMP for instance)
	 * 
	 * @return
	 */
	public boolean isMultiThreading() {
		return (type & Constants.MULTI_THREADING) != 0;
	}
	
	/***
	 * Check if the application is a hybrid program (MPI+OpenMP)
	 * 
	 * @return
	 */
	public boolean isHybrid() {
		return (isMultiProcess() && isMultiThreading());
	}

	@Override
	public long getLong(long position) throws IOException {
		return masterBuff.getLong(position);
	}

	@Override
	public int getInt(long position) throws IOException {
		return masterBuff.getInt(position);
	}

	@Override
	public double 	getDouble(long position) throws IOException {
		return masterBuff.getDouble(position);
	}

	/***
	 * Disposing native resources
	 */
	public void dispose() {
		if (masterBuff != null)
			masterBuff.dispose();

		if (file != null) {
			try {
				// ------------------------------------------------------
				// need to close the file and its file channel
				// somehow this can free the memory
				// ------------------------------------------------------
				file.close();
			} catch (IOException e) {
				// nothing to do
			}
		}
	}

	@Override
	public long getMinLoc(int rank) {
		final long[] rankOffsets = getOffsets();
		
		if (rank >= rankOffsets.length) {
			throw new RuntimeException("File DB2: incorrect rank: " + rank +" (bigger than " + rankOffsets.length+")");
		}
		return rankOffsets[rank] + headerSize;
	}

	@Override
	public long getMaxLoc(int rank) {
		final long[] rankOffsets = getOffsets();
		return ( (rank+1<getNumberOfRanks())? 
				rankOffsets[rank+1] : masterBuff.size()-1 )
				- recordSz;
	}

	@Override
	public IdTupleType getIdTupleTypes() {
		if (listIdTupleTypes != null)
			return listIdTupleTypes;
		
		listIdTupleTypes = new IdTupleType();
		listIdTupleTypes.initDefaultTypes();
		
		return listIdTupleTypes;
	}

	@Override
	public boolean hasGPU() {
		return false;
	}
	
	@Override
	public boolean isGPU(int rank) {
		if (!isMultiThreading())
			return false;
		IdTuple idTuple = listIdTuples.get(rank);
		long thread = idTuple.getIndex(IdTupleType.KIND_THREAD);
		return thread >= 500;
	}
}
