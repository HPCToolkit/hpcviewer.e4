package edu.rice.cs.hpctraceviewer.data.version4;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.rice.cs.hpcdata.db.IFileDB;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.version2.FileDB2;
import edu.rice.cs.hpcdata.db.version4.DataSummary;
import edu.rice.cs.hpcdata.db.version4.DataTrace;
import edu.rice.cs.hpcdata.experiment.BaseExperiment;


/********************************************************************
 * 
 * Class to manage access to extended database file
 * This class uses a compact version of database format (version 3)
 * and not compatible with the old one.<br/>
 * See {@link FileDB2} for accessing the older format
 *
 ********************************************************************/
public class FileDB4 implements IFileDB 
{
	private DataTrace dataTrace;
	private DataSummary dataSummary;
	
	
	/****
	 * @deprecated method to open a database file.<br/>
	 * We provide this method just for compatibility purpose. Please use
	 * the recent method : 
	 * 
	 * {@link open(String)}
	 * @see edu.rice.cs.hpcdata.db.IFileDB#open(java.lang.String, int, int)
	 */
	@Override
	public void open(String filename, int headerSize, int recordSize)
			throws IOException 
	{
	}
	
	/***
	 * Method to open files of trace database such as trace.db and threads.db
	 * 
	 * @param directory
	 * @throws IOException
	 */
	public void open(DataSummary dataSummary, String directory) throws IOException
	{
		this.dataSummary = dataSummary;

		String filename = directory + File.separatorChar + BaseExperiment.getDefaultDbTraceFilename();
		dataTrace = new DataTrace();
		dataTrace.open(filename);
	}

	@Override
	public int getNumberOfRanks() {
		return dataTrace.getNumberOfRanks();
	}

	@Override
	public String[] getRankLabels() {
		return dataSummary.getStringLabelIdTuples();
	}

	@Override
	public long[] getOffsets() {
		System.out.println("ERROR, shouldn't be called!");
		return null;
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
		return dataSummary.getParallelismLevels();
	}

	
	@Override
	public boolean isGPU(int rank) {
		List<IdTuple> listId = dataSummary.getIdTuple();
		IdTuple idTuple = listId.get(rank);
		return idTuple.isGPU(getIdTupleTypes());
	}
	
	
	@Override
	public long getMinLoc(int rank) {
		// another redirection: look at id tuple to get the profile number
		// then, search the offset of this profile number
		
		List<IdTuple> listId = dataSummary.getIdTuple();
		int profileNum = listId.get(rank).getProfileNum();
		return dataTrace.getOffset(profileNum);
	}

	@Override
	public long getMaxLoc(int rank) {
		// another redirection: look at id tuple to get the profile number
		// then, search the offset of this profile number, and its length
		
		List<IdTuple> listId = dataSummary.getIdTuple();
		int profileNum = listId.get(rank).getProfileNum();
		return dataTrace.getOffset(profileNum) + dataTrace.getLength(profileNum);
	}

	@Override
	public void dispose() {
		try {
			dataTrace.dispose();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<IdTuple> getIdTuple(IdTupleOption option) {
		return dataSummary.getIdTuple(option);
	}

	@Override
	public IdTupleType getIdTupleTypes() {
		return dataSummary.getIdTupleType();
	}

	@Override
	public boolean  hasGPU() {
		return dataSummary.hasGPU();
	}
}
