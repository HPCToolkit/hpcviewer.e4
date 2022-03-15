package edu.rice.cs.hpctraceviewer.data.version4;

import java.io.IOException;
import java.util.List;

import org.slf4j.LoggerFactory;

import edu.rice.cs.hpcdata.db.IFileDB;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.version2.FileDB2;
import edu.rice.cs.hpcdata.db.version4.DataSummary;
import edu.rice.cs.hpcdata.db.version4.DataTrace;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.trace.TraceAttribute;


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
	private final DataTrace   dataTrace;
	private final DataSummary dataSummary;
	private final IExperiment experiment;
	
	/*****
	 * Creation of FileDB for sparse database version 4.
	 * 
	 * @param dataSummary
	 * 			The object of profile.db parser
	 * 			
	 * @throws IOException
	 */
	public FileDB4(IExperiment experiment, DataSummary dataSummary) throws IOException {
		this.dataSummary = dataSummary;
		this.dataTrace   = new DataTrace();
		this.experiment  = experiment;
	}


	/****
	 * Open trace file
	 * @param directory
	 * 			The directory of the database
	 * @param headerSize
	 * 			unused
	 * @param recordSize
	 * 			unused
	 */
	@Override
	public void open(String directory, int headerSize, int recordSize)
			throws IOException 
	{
		dataTrace.open(directory);
		
		TraceAttribute attributes = new TraceAttribute();
		attributes.dbTimeMin = dataTrace.getMinTime();
		attributes.dbTimeMax = dataTrace.getMaxTime();
		attributes.maxDepth  = experiment.getMaxDepth();
		
		experiment.setTraceAttribute(attributes);
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
		throw new RuntimeException("ERROR, shouldn't be called!");
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
		
		return dataTrace.getOffset(rank);
	}

	@Override
	public long getMaxLoc(int rank) {
		// another redirection: look at id tuple to get the profile number
		// then, search the offset of this profile number, and its length
		
		return dataTrace.getOffset(rank) + dataTrace.getLength(rank);
	}

	@Override
	public void dispose() {
		try {
			dataTrace.dispose();
		} catch (IOException e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error(e.getMessage());
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
