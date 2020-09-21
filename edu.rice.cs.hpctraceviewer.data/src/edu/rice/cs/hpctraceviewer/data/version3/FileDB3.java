package edu.rice.cs.hpctraceviewer.data.version3;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.rice.cs.hpc.data.db.IdTuple;
import edu.rice.cs.hpc.data.db.version3.DataThread;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.extdata.FileDB2;
import edu.rice.cs.hpc.data.experiment.extdata.IFileDB;

/********************************************************************
 * 
 * Class to manage access to extended database file
 * This class uses a compact version of database format (version 3)
 * and not compatible with the old one.<br/>
 * See {@link FileDB2} for accessing the older format
 *
 ********************************************************************/
public class FileDB3 implements IFileDB 
{
	private DataTrace dataTrace;
	private DataThread dataThread;
	
	
	/****
	 * @deprecated method to open a database file.<br/>
	 * We provide this method just for compatibility purpose. Please use
	 * the recent method : 
	 * 
	 * {@link open(String)}
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IFileDB#open(java.lang.String, int, int)
	 */
	@Override
	public void open(String filename, int headerSize, int recordSize)
			throws IOException 
	{
		File file  = new File(filename);
		String dir = file.getParent();
		open(dir);
	}
	
	/***
	 * Method to open files of trace database such as trace.db and threads.db
	 * 
	 * @param directory
	 * @throws IOException
	 */
	public void open(String directory) throws IOException
	{
		String filename = directory + File.separatorChar + BaseExperiment.getDefaultDbTraceFilename();
		dataTrace = new DataTrace();
		dataTrace.open(filename);
		
		filename = directory + File.separatorChar + 
				BaseExperiment.getDefaultDatabaseName(BaseExperiment.Db_File_Type.DB_THREADS);
		dataThread = new DataThread();
		dataThread.open(filename);
	}

	@Override
	public int getNumberOfRanks() {
		return dataTrace.getNumberOfRanks();
	}

	@Override
	public String[] getRankLabels() {
		int numLevels = dataThread.getParallelismLevel();
		int []ranks   = dataThread.getParallelismRank();
		int num_ranks  = ranks.length / numLevels;
		String []rank_label = new String[num_ranks]; 

		StringBuffer sbRank = new StringBuffer();
		for (int i=0; i<num_ranks; i++)
		{
			sbRank.setLength(0);
			for(int j=0; j<numLevels; j++)
			{
				int k = i*numLevels + j;
				sbRank.append( String.valueOf(ranks[k]) );
				if (j == numLevels-1)
				{
					rank_label[i] = sbRank.toString();
				} else
				{
					sbRank.append(".");
				}
			}
		}
		return rank_label;
	}

	@Override
	public long[] getOffsets() {
		return dataTrace.getOffsets();
	}

	@Override
	public long getLong(long position) throws IOException {

		return dataTrace.getLong(position);
	}

	@Override
	public int getInt(long position) throws IOException {
		return dataTrace.getInt(position);
	}

	@Override
	public double getDouble(long position) throws IOException {
		return dataTrace.getDouble(position);
	}

	@Override
	public int getParallelismLevel() {
		return dataThread.getParallelismLevel();
	}

	@Override
	public long getMinLoc(int rank) {
		long []offsets = dataTrace.getOffsets();
		return offsets[rank];
	}

	@Override
	public long getMaxLoc(int rank) {
		return getMinLoc(rank) + dataTrace.getLength(rank) - DataTrace.RECORD_ENTRY_SIZE;
	}

	@Override
	public void dispose() {
		try {
			dataTrace.dispose();
			dataThread.dispose();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<IdTuple> getIdTuple() {
		// TODO Auto-generated method stub
		return null;
	}

}
