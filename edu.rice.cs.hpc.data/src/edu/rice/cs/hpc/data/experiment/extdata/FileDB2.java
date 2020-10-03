package edu.rice.cs.hpc.data.experiment.extdata;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import edu.rice.cs.hpc.data.db.IdTuple;
import edu.rice.cs.hpc.data.db.IdTupleType;
import edu.rice.cs.hpc.data.util.Constants;
import edu.rice.cs.hpc.data.util.LargeByteBuffer;
import edu.rice.cs.hpc.data.util.Util;

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
	private String valuesX[];
	private long offsets[];
	
	private int recordSz;
	private int headerSize;
	
	private RandomAccessFile file; 
	
	private List<IdTuple> listIdTuples;

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
		listIdTuples = new ArrayList<IdTuple>(numFiles);
		
		long current_pos = Constants.SIZEOF_INT * 2;
		
		// get the procs and threads IDs
		for(int i=0; i<numFiles; i++) {

			final int proc_id = masterBuff.getInt(current_pos);
			current_pos += Constants.SIZEOF_INT;
			final int thread_id = masterBuff.getInt(current_pos);
			current_pos += Constants.SIZEOF_INT;
			
			offsets[i] = masterBuff.getLong(current_pos);
			current_pos += Constants.SIZEOF_LONG;
			
			IdTuple tuple = new IdTuple(i, getParallelismLevel());
			
			if (getParallelismLevel() == 0) {
				// sequential program
				listIdTuples.add(tuple);
				continue;
			}
			//--------------------------------------------------------------------
			// adding list of x-axis 
			//--------------------------------------------------------------------			
			
			String x_val;
			if (this.isHybrid()) 
			{
				x_val = String.valueOf(proc_id) + "." + String.valueOf(thread_id);
				
				tuple.kind[0]  = IdTupleType.KIND_RANK;
				tuple.index[0] = proc_id;
				
				tuple.kind[1]  = IdTupleType.KIND_THREAD;
				tuple.index[1] = thread_id;
				
			} else if (isMultiProcess()) 
			{
				x_val = String.valueOf(proc_id);					
				
				tuple.kind[0]  = IdTupleType.KIND_RANK;
				tuple.index[0] = proc_id;
			} else if (isMultiThreading()) 
			{
				x_val = String.valueOf(thread_id);
				
				tuple.kind[0]  = IdTupleType.KIND_THREAD;
				tuple.index[0] = thread_id;
			} else {
				// temporary fix: if the application is neither hybrid nor multiproc nor multithreads,
				// we just print whatever the order of file name alphabetically
				// this is not the ideal solution, but we cannot trust the value of proc_id and thread_id
				x_val = String.valueOf(i);
				
				tuple.kind[0]  = IdTupleType.KIND_RANK;
				tuple.index[0] = i;
			}
			valuesX[i] = x_val;
			listIdTuples.add(tuple);
		}
	}

	@Override
	public int 	getParallelismLevel()
	{
		return Util.countSetBits(type);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public long getMinLoc(int rank) {
		final long offsets[] = getOffsets();
		long loc = -1;
		
		if (rank < offsets.length) {
			loc = offsets[rank] + headerSize;
		} else {
			throw new RuntimeException("File DB2: incorrect rank: " + rank +" (bigger than " + offsets.length+")");
		}
		return loc;
	}

	@Override
	public long getMaxLoc(int rank) {
		final long offsets[] = getOffsets();
		long maxloc = ( (rank+1<getNumberOfRanks())? 
				offsets[rank+1] : masterBuff.size()-1 )
				- recordSz;
		return maxloc;
	}
}
