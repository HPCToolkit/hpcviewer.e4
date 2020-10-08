package edu.rice.cs.hpctraceviewer.data.version2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.rice.cs.hpc.data.db.IdTuple;
import edu.rice.cs.hpc.data.experiment.extdata.FileDB2;
import edu.rice.cs.hpc.data.experiment.extdata.IBaseData;
import edu.rice.cs.hpc.data.experiment.extdata.IFileDB;
import edu.rice.cs.hpc.data.experiment.extdata.IFilteredData;
import edu.rice.cs.hpc.data.experiment.extdata.IFileDB.IdTupleOption;
import edu.rice.cs.hpc.data.trace.FilterSet;


/******************************************************************
 * 
 * Filtered version for accessing a raw data file
 * A data file can either thread level data metric, or trace data
 * @see IBaseData
 * @see AbstractBaseData
 * @see FileDB2
 *******************************************************************/
public class FilteredBaseData extends AbstractBaseData implements IFilteredData {

	private FilterSet filter;
	private int []indexes;
	private List<IdTuple> filteredIdTuples;

	/*****
	 * construct a filtered data
	 * The user is responsible to make sure the filter has been set with setFilters()
	 * 
	 * @param filename
	 * @param headerSize
	 * @throws IOException
	 */
	public FilteredBaseData(IFileDB baseDataFile, int headerSize, int recordSz) throws IOException 
	{
		super( baseDataFile);
		filter = new FilterSet();
	}

	/****
	 * start to filter the ranks
	 */
	private void applyFilter() {
		if (baseDataFile == null)
			throw new RuntimeException("Fatal error: cannot find data.");
		
		List<IdTuple> listIdTuples = baseDataFile.getIdTuple(IdTupleOption.BRIEF);

		filteredIdTuples = null;

		ArrayList<Integer> lindexes = new ArrayList<Integer>();

		if (filter.hasAnyFilters()) {
			for (int i = 0; i < listIdTuples.size(); i++) {
				if (filter.include(listIdTuples.get(i)))
					lindexes.add(i);
			}
			//Convert ArrayList to array
			indexes = new int[lindexes.size()];

			for (int i = 0; i < indexes.length; i++) {
				indexes[i] = lindexes.get(i);
			}
		} else {
			// no glob pattern to filter
			// warning: not optimized code
			indexes = new int[listIdTuples.size()];
			for(int i=0; i<listIdTuples.size(); i++) {
				indexes[i] = i;
			}
		}
	}
	
	/****
	 * set patterns to filter ranks
	 * @param filters
	 */
	public void setFilter(FilterSet filter) {
		this.filter = filter;
		applyFilter();
	}
	
	
	public FilterSet getFilter() {
		return filter;
	}

	
	@Override
	public List<IdTuple> getListOfIdTuples(IdTupleOption option) {
		if (filteredIdTuples == null) {
			filteredIdTuples = new ArrayList<IdTuple>(indexes.length);
			List<IdTuple> listDensedIdTuples = baseDataFile.getIdTuple(option);
			
			for(int i=0; i<indexes.length; i++) {
				IdTuple tuple = listDensedIdTuples.get(indexes[i]);
				filteredIdTuples.add(tuple);
			}
		}
		return filteredIdTuples;
	}


	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getNumberOfRanks()
	 */
	public int getNumberOfRanks() {
		return indexes.length;
	}
	

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getMinLoc(int)
	 */
	public long getMinLoc(int rank) {
		int filteredRank = indexes[rank];
		return baseDataFile.getMinLoc(filteredRank);
/*		final long offsets[] = baseDataFile.getOffsets();
		return offsets[filteredRank] + headerSize;*/
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.extdata.IBaseData#getMaxLoc(int)
	 */
	public long getMaxLoc(int rank) {
		int filteredRank = indexes[rank];
		return baseDataFile.getMaxLoc(filteredRank);
/*		final long offsets[] = baseDataFile.getOffsets();
		long maxloc = ( (filteredRank+1<baseDataFile.getNumberOfRanks())? 
				offsets[filteredRank+1] : baseDataFile.getMasterBuffer().size()-SIZE_OF_END_OF_FILE_MARKER )
				- getRecordSize();
		return maxloc;*/
	}

	@Override
	public boolean isGoodFilter() {
		return getNumberOfRanks() > 0;
	}

	@Override
	public int getFirstIncluded() {
		if (indexes == null || indexes.length==0)
			return 0;
		
		return indexes[0];
	}

	@Override
	public int getLastIncluded() {
		if (indexes == null || indexes.length==0)
			return 0;
		
		return indexes[indexes.length-1];
	}

	@Override
	public boolean isDenseBetweenFirstAndLast() {
		if (indexes == null || indexes.length==0)
			return true;
		
		return indexes[indexes.length-1]-indexes[0] == indexes.length-1;
	}

}
