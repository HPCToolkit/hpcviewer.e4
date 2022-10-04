package edu.rice.cs.hpcdata.db.version4;

import java.io.IOException;
import java.util.List;

import edu.rice.cs.hpcdata.db.IFileDB;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.db.version2.FileDB2;
import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.visitors.TraceScopeVisitor;


/********************************************************************
 * 
 * Class to manage access to extended database file like <code>trace.db</code> 
 * <p>
 * This class uses a compact version of database format (version 4)
 * and not compatible with the old one.</p>
 * 
 * @see {@link FileDB2} for accessing the older format
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
	 * @param experiment
	 * 			The experiment database
	 * @param dataSummary
	 * 			The object of profile.db parser
	 * 			
	 * @throws IOException
	 */
	public FileDB4(IExperiment experiment, DataSummary dataSummary) {
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
		
		var rootCCT = ((BaseExperiment)experiment).getRootScope(RootScopeType.CallingContextTree);
		
		// If we already computed the call-path map, we do not do it again.
		// It's harmless to recompute but it such a waste of CPU resources.
		
		if (rootCCT != null && experiment.getScopeMap() == null) {
			// needs to gather info about cct id and its depth
			// this is needed for traces
			TraceScopeVisitor visitor = new TraceScopeVisitor();
			rootCCT.dfsVisitScopeTree(visitor);
			
			experiment.setMaxDepth(visitor.getMaxDepth());
			experiment.setScopeMap(visitor.getCallPath());
		}

		var attributes = experiment.getTraceAttribute();
		
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
		// shouldn't be called for v4
		return new long[0];
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
		int profile = listId.get(rank).getProfileIndex() - 1;

		return dataTrace.getOffset(profile);
	}

	@Override
	public long getMaxLoc(int rank) {
		// another redirection: look at id tuple to get the profile number
		// then, search the offset of this profile number, and its length
		List<IdTuple> listId = dataSummary.getIdTuple();
		int profile = listId.get(rank).getProfileIndex() - 1;
		
		return dataTrace.getOffset(profile) + dataTrace.getLength(profile) - dataTrace.getRecordSize();
	}

	@Override
	public void dispose() {
		try {
			dataTrace.dispose();
		} catch (IOException e) {
			// unable to dispose
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
