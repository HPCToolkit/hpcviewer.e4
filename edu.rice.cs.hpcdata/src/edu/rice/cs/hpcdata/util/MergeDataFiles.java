package edu.rice.cs.hpcdata.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;



/*****
 * Adaptation of TraceCompactor from traceviewer for more general purpose
 *  
 * Example of input files: 
 * 
 * s3d_f90.x-000002-000-a8c01d31-26093.hpctrace
 * 1.fft-000142-000-a8c0a230-23656.metric-db
 * 
 * Example of output file:
 * 
 * data.hpctrace.single
 * data.metric-db.single
 * 
 * @author laksono 
 *
 */
public class MergeDataFiles {
	
	private static final int PAGE_SIZE_GUESS = 4096;
	
	private static final int PROC_POS = 5;
	private static final int THREAD_POS = 4;
	
	public enum MergeDataAttribute {SUCCESS_MERGED, SUCCESS_ALREADY_CREATED, FAIL_NO_DATA};
	
	/***
	 * create a single file from multiple data files
	 * 
	 * @param directory
	 * @param globInputFile: glob pattern
	 * @param outputFile: output filename
	 * 
	 * @return
	 * @throws IOException
	 */
	static public MergeDataAttribute merge(File directory, String globInputFile, String outputFile, IProgressReport progress)
			throws IOException, FileNotFoundException {
		
		final int last_dot = globInputFile.lastIndexOf('.');
		final String suffix = globInputFile.substring(last_dot);

		final File fout = new File(outputFile);
		
		// check if the file already exists
		if (fout.canRead() )
		{
			if (isMergedFileCorrect(outputFile))			
				return MergeDataAttribute.SUCCESS_ALREADY_CREATED;
			// the file exists but corrupted. In this case, we have to remove and create a new one
			throw new RuntimeException("MT file corrupted.");
		}
		
		// check if the files in glob patterns is correct
		File[] file_metric = directory.listFiles( new Util.FileThreadsMetricFilter(globInputFile) );
		if (file_metric == null || file_metric.length<1)
			return MergeDataAttribute.FAIL_NO_DATA;
		
		FileOutputStream fos = new FileOutputStream(outputFile);
		DataOutputStream dos = new DataOutputStream(fos);
		
		//-----------------------------------------------------
		// 1. write the header:
		//  int type (0: unknown, 1: mpi, 2: openmp, 3: hybrid, ...
		//	int num_files
		//-----------------------------------------------------

		int type = 0;
		dos.writeInt(type);
		
		progress.begin("Merging data files ...", file_metric.length);
		
		// on linux, we have to sort the files
		java.util.Arrays.sort(file_metric);
		
		dos.writeInt(file_metric.length);
		
		final long num_metric_header = 2 * Constants.SIZEOF_INT; // type of app (4 bytes) + num procs (4 bytes) 
		final long num_metric_index  = file_metric.length * (Constants.SIZEOF_LONG + 2 * Constants.SIZEOF_INT );
		long offset = num_metric_header + num_metric_index;

		int name_format = 0;  // FIXME hack:some hpcprof revisions have different format name !!
				
		//-----------------------------------------------------
		// 2. Record the process ID, thread ID and the offset 
		//   It will also detect if the application is mp, mt, or hybrid
		//	 no accelator is supported
		//  for all files:
		//		int proc-id, int thread-id, long offset
		//-----------------------------------------------------
		for(int i = 0; i < file_metric.length; ++i)
		{
			//get the core number and thread number
			final String filename = file_metric[i].getName();
			final int last_pos_basic_name = filename.length() - suffix.length();
			final String basic_name = file_metric[i].getName().substring(0, last_pos_basic_name);
			String []tokens = basic_name.split("-");
			
			final int num_tokens = tokens.length;
			if (num_tokens < PROC_POS)
				// if it is wrong file with the right extension, we skip 
				continue;
			
			int proc ;
			try {
				proc = Integer.parseInt(tokens[name_format + num_tokens-PROC_POS]);
			} catch (NumberFormatException e) {
				// old version of name format
				name_format = 1; 
				proc = Integer.parseInt(tokens[name_format + num_tokens-PROC_POS]);
			}
			dos.writeInt(proc);
			if (proc != 0)
				type |= Constants.MULTI_PROCESSES;
			
			final int thread = Integer.parseInt(tokens[name_format + num_tokens-THREAD_POS]);
			dos.writeInt(thread);
			if (thread != 0)
				type |= Constants.MULTI_THREADING;
			

			dos.writeLong(offset);
			offset += file_metric[i].length();

		}
		
		//-----------------------------------------------------
		// 3. Copy all data from the multiple files into one file
		//-----------------------------------------------------
		for(int i = 0; i < file_metric.length; ++i) {
			DataInputStream dis = new DataInputStream(new FileInputStream(file_metric[i]));
			byte[] data = new byte[PAGE_SIZE_GUESS];
			
			int numRead = dis.read(data);
			while(numRead > 0) {
				dos.write(data, 0, numRead);
				numRead = dis.read(data);
			}
			dis.close();
			
			progress.advance();
		}		
		insertMarker(dos);
		
		dos.close();
		
		//-----------------------------------------------------
		// 4. FIXME: write the type of the application
		//  	the type of the application is computed in step 2
		//		Ideally, this step has to be in the beginning !
		//-----------------------------------------------------
		RandomAccessFile f = new RandomAccessFile(outputFile, "rw");
		f.writeInt(type);
		f.close();
		
		//-----------------------------------------------------
		// 5. remove old files
		//-----------------------------------------------------
		removeFiles(file_metric);
		
		progress.end();
		
		return MergeDataAttribute.SUCCESS_MERGED;

	}
	
	// pat2 7/24/13: The marker used to be:
	//static private long MARKER_END_MERGED_FILE = 0xDEADF00D;
	// but Java sign-extends the int to a long and it becomes
	// 0xFFFFFFFFDEADF00D NOT 0x00000000DEADF00D, like you'd probably guess.
	// Making it explicitly what it was implicitly before to avoid 
	// compatibility issues.
	/** Magic marker for the end of the file **/
	static private long MARKER_END_MERGED_FILE = 0xFFFFFFFFDEADF00DL;
	
	/***
	 * insert a marker at the end of the file
	 * @param dos: output stream. It has to be the end of the file
	 * @throws IOException
	 */
	static private void insertMarker(DataOutputStream dos) throws IOException
	{
		dos.writeLong(MARKER_END_MERGED_FILE);
	}
	
	
	/***
	 * Check if a file is a good merged file
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	static private boolean isMergedFileCorrect(String filename) throws IOException
	{
		final RandomAccessFile f = new RandomAccessFile(filename, "r");
		boolean isCorrect = false;
		
		final long pos = f.length() - Constants.SIZEOF_LONG;
		if (pos>0) {
			f.seek(pos);
			final long marker = f.readLong();
			isCorrect = (marker == MARKER_END_MERGED_FILE);
		}
		f.close();
		return isCorrect;
	}
	
	static private boolean removeFiles(File files[])
	{
		boolean success = true;
		
		for(File file: files) {
			success &= file.delete();
		}
		
		return success;
	}
}
