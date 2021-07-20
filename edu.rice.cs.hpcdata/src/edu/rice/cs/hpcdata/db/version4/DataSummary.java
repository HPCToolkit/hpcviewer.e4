
package edu.rice.cs.hpcdata.db.version4;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.experiment.extdata.IFileDB;
import edu.rice.cs.hpcdata.experiment.extdata.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.experiment.metric.MetricValueSparse;

/*********************************************
 * 
 * Class to handle summary.db file generated by hpcprof
 *
 *********************************************/
public class DataSummary extends DataCommon 
{
	// --------------------------------------------------------------------
	// constants
	// --------------------------------------------------------------------
	private final static String HEADER_MAGIC_STR  = "HPCPROF-profdb__";
	private static final int    METRIC_VALUE_SIZE = 8 + 2;
	private static final int    CCT_RECORD_SIZE   = 4 + 8;
	private static final int    MAX_LEVELS        = 18;
	
	private static final int    PROFILE_SUMMARY_INDEX = 0;
	public static final int     PROFILE_SUMMARY       = -1;
	
	// --------------------------------------------------------------------
	// object variable
	// --------------------------------------------------------------------
	
	private final IdTupleType idTupleTypes;
	private RandomAccessFile file;
	
	/*** cache variables so that we don't need to read again and again  ***/
	/*** cache the content of the file for a particular profile number  ***/
	private ByteBuffer byteBufferCache;
	
	/*** Current cached data of a certain profile ***/
	private int profileNumberCache;
	
	private List<IdTuple>  listIdTuple, listIdTupleShort;
	private List<ProfInfo> listProfInfo;
	
	/*** mapping from profile number to the sorted order*/
	private Map<Integer, Integer> mapProfileToOrder;
		
	protected boolean optimized = true;

	/** Number of parallelism level or number of levels in hierarchy */
	private int numLevels;
	
	private double[] labels;
	private String[] strLabels;
	private boolean hasGPU;
	
	public DataSummary(IdTupleType idTupleTypes) {
		this.idTupleTypes = idTupleTypes;
	}
	
	// --------------------------------------------------------------------
	// Public methods
	// --------------------------------------------------------------------
	
	/***
	 *  <p>Opening for data summary metric file</p>
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpcdata.db.version4.DataCommon#open(java.lang.String)
	 */
	@Override
	public void open(final String filename)
			throws IOException
	{
		super.open(filename);
		file = new RandomAccessFile(filename, "r");
	}
	

	@Override
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.db.DataCommon#printInfo(java.io.PrintStream)
	 */
	public void printInfo( PrintStream out)
	{
		super.printInfo(out);
		
		// print list of id tuples
		for(IdTuple idt: listIdTuple) {
			System.out.println(idt);
		}
		System.out.println();

		ListCCTAndIndex list = null;
		
		try {
			list = getCCTIndex();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		// print random metrics
		int len = Math.min(100, list.listOfId.length);
		
		for (int i=0; i<len; i++)
		{
			int cct = list.listOfId[i];
			out.format("[%5d] ", cct);
			printMetrics(out, cct);
		}
	}

	public ListCCTAndIndex getCCTIndex() 
			throws IOException {
		
		ProfInfo info = listProfInfo.get(PROFILE_SUMMARY_INDEX);
		
		// -------------------------------------------
		// read the cct context
		// -------------------------------------------
		
		long positionCCT = info.offset   + 
				   		   info.num_vals * METRIC_VALUE_SIZE;
		int numBytesCCT  = info.num_nz_contexts * CCT_RECORD_SIZE;
		
		ListCCTAndIndex list = new ListCCTAndIndex();
		list.listOfdIndex = new long[info.num_nz_contexts];
		list.listOfId = new int[info.num_nz_contexts];
		
		MappedByteBuffer buffer = file.getChannel().map(MapMode.READ_ONLY, positionCCT, numBytesCCT);
		
		for(int i=0; i<info.num_nz_contexts; i++) {
			list.listOfId[i] = buffer.getInt();
			list.listOfdIndex[i] = buffer.getLong();
		}
		return list;
	}
	
	
	/***
	 * Retrieve the list of tuple IDs.
	 * @return {@code List<IdTuple>} List of Tuple
	 */
	public List<IdTuple> getIdTuple() {
		if (optimized)
			return getIdTuple(IFileDB.IdTupleOption.BRIEF);
		
		return getIdTuple(IFileDB.IdTupleOption.COMPLETE);
	}
	
	
	/****
	 * Retrieve the list of id tuples
	 * 
	 * @param shortVersion: true if we want to the short version of the list
	 * 
	 * @return {@code List<IdTuple>}
	 */
	public List<IdTuple> getIdTuple(IdTupleOption option) {
		switch(option) {
		case COMPLETE: 
			return listIdTuple;

		case BRIEF:
		default:
			return listIdTupleShort;
		}
	}
	
	
	
	/****
	 * Get the value of a specific profile with specific cct and metric id
	 * 
	 * @param profileNum
	 * @param cctId
	 * @param metricId
	 * @return
	 * @throws IOException
	 */
	public double getMetric(int profileNum, int cctId, int metricId) 
			throws IOException
	{
		List<MetricValueSparse> listValues = getMetrics(profileNum, cctId);
		
		if (listValues != null) {
			
			// TODO ugly temporary code
			// We need to grab a value directly from the memory instead of searching O(n)
			
			for (MetricValueSparse mvs: listValues) {
				if (mvs.getIndex() == metricId) {
					return mvs.getValue();
				}
			}
		}
		return 0.0d;
	}
	
	/**********
	 * Reading a set of metrics from the file for a given CCT 
	 * This method does not support concurrency. The caller is
	 * responsible to handle mutual exclusion.
	 * 
	 * @param cct_id
	 * @return List of MetricValueSparse
	 * @throws IOException
	 */
	public List<MetricValueSparse> getMetrics(int cct_id) 
			throws IOException
	{		
		return getMetrics(PROFILE_SUMMARY, cct_id);
	}
	
	
	/****
	 * Retrieve the list of metrics for a certain profile number and a given cct id
	 * 
	 * @param profileNum The profile number. For summary profile, it equals to {@code PROFILE_SUMMARY}
	 * @param cct_id the cct id
	 * @return List of MetricValueSparse
	 * @throws IOException
	 */
	public List<MetricValueSparse> getMetrics(int profileNum, int cct_id) 
			throws IOException 
	{	
		ProfInfo info ;
		if (profileNum == PROFILE_SUMMARY) {
			info =listProfInfo.get(PROFILE_SUMMARY_INDEX);
		} else {
			IdTuple idt = listIdTuple.get(profileNum);
			info = listProfInfo.get(idt.profileNum);
		}
		
		if (profileNumberCache != profileNum || byteBufferCache == null) {
			// -------------------------------------------
			// read the cct context
			// -------------------------------------------
			
			long positionCCT = info.offset + info.num_vals * METRIC_VALUE_SIZE;
			int numBytesCCT  = (info.num_nz_contexts+1)    * CCT_RECORD_SIZE;
			
			FileChannel channel = file.getChannel();
			byte []arrayBytes   = new byte[numBytesCCT];
			
			byteBufferCache = ByteBuffer.wrap(arrayBytes);
			
			channel.position(positionCCT);
			channel.read(byteBufferCache);
			
			profileNumberCache = profileNum;
		}

		long []indexes = newtonSearch(cct_id, 0, info.num_nz_contexts, byteBufferCache);

		if (indexes == null)
			// the cct id is not found or the cct has no metrics. Should we return null or empty list?
			return null;
		
		// -------------------------------------------
		// initialize the metrics
		// -------------------------------------------

		int numMetrics   = (int) (indexes[1]-indexes[0]);
		int numBytes     = (int) numMetrics * METRIC_VALUE_SIZE;
		
		ArrayList<MetricValueSparse> values = new ArrayList<MetricValueSparse>(numMetrics);
		
		// -------------------------------------------
		// read the metrics
		// -------------------------------------------
		long positionMetrics = info.offset + indexes[0] * METRIC_VALUE_SIZE;
		file.seek(positionMetrics);
		
		byte []metricBuffer = new byte[numBytes];
		file.readFully(metricBuffer);
		ByteBuffer byteBuffer = ByteBuffer.wrap(metricBuffer);

		for(int i=0; i<numMetrics; i++) {
			double value = byteBuffer.getDouble();
			int metricId = (int) (0xffff & byteBuffer.getShort());
			
			MetricValueSparse mvs = new MetricValueSparse(metricId, (float) value);
			values.add(mvs);
		}
		
		return values;
	}
	
	
	/****
	 * Retrieve the list of id tuple label in string
	 * @return String[]
	 */
	public String[] getStringLabelIdTuples() {
		if (strLabels == null) {
			strLabels = new String[listIdTupleShort.size()];
			
			for(int i=0; i<listIdTupleShort.size(); i++) {
				IdTuple idt  = listIdTupleShort.get(i);
				strLabels[i] = idt.toString();
			}		
		}
		return strLabels;
	}
	
	
	/****
	 * Retrieve the list of id tuple representation in double.
	 * For OpenMP programs, it returns the list of 1, 2, 3,...
	 * For MPI+OpenMP programs, it returns the list of 1.0, 1.1, 1.2, 2.0, 2.1, ... 
	 * @return double[]
	 */
	public double[] getDoubleLableIdTuples() {
		if (labels == null) {
			labels    = new double[listIdTupleShort.size()];
			
			for(int i=0; i<listIdTupleShort.size(); i++) {
				IdTuple idt  = listIdTupleShort.get(i);
				String label = idt.toLabel();
				labels[i]    = label == null? 0 : Double.valueOf(label);
			}		
		}
		return labels;
	}
	
	
	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.db.DataCommon#dispose()
	 */
	@Override
	public void dispose() throws IOException
	{
		file.close();
		super.dispose();
	}

	/***
	 * Get the profile id based from the rank "order".
	 * As the data is not sorted based on id tuple, this method will return
	 * the profile index given the index of sorted id tuples.
	 * 
	 * @param orderNumber {@code int} the index
	 * @return {@code int} the profile index, used fro accessing the database
	 */
	public int getProfileIndexFromOrderIndex(int orderNumber) {
		return mapProfileToOrder.get(orderNumber);
	}

	
	
	// --------------------------------------------------------------------
	// Protected methods
	// --------------------------------------------------------------------
	
	@Override
	protected boolean isTypeFormatCorrect(long type) {
		return type==1;
	}

	@Override
	protected boolean isFileHeaderCorrect(String header) {
		return header.equals(HEADER_MAGIC_STR);
	}

	@Override
	protected boolean readNextHeader(FileChannel input, DataSection []sections) 
			throws IOException
	{
		readProfInfo(input, sections[0]);
		readIdTuple (input, sections[1]);
		
		long nextPosition = sections[1].offset + getMultiplyOf8( sections[1].size);
		input.position(nextPosition);
		
		return true;
	}
	
	// --------------------------------------------------------------------
	// Private methods
	// --------------------------------------------------------------------

		
	/***
	 * read the list of id tuple, sort based on the rank and level, then compute
	 * the abbreviation version of id tuples.
	 * 
	 * @param input FileChannel
	 * @throws IOException
	 */
	private void readIdTuple(FileChannel input, DataSection idTupleSection) 
			throws IOException
	{
		input.position(idTupleSection.offset);

		// -----------------------------------------
		// 1. Read the id tuples section from the thread.db
		// -----------------------------------------
				
		listIdTuple = new ArrayList<IdTuple>((int) numItems-1);
		listIdTupleShort = new ArrayList<IdTuple>((int) numItems-1);
		
		ByteBuffer buffer = ByteBuffer.allocate((int) idTupleSection.size);
		
		int numBytes      = input.read(buffer);
		assert (numBytes > 0);

		buffer.flip();
		
		@SuppressWarnings("unchecked")
		Map<Long, Integer> []mapLevelToHash = new HashMap[MAX_LEVELS];
		
		long []minIndex = new long[MAX_LEVELS];
		long []maxIndex = new long[MAX_LEVELS];
		
		numLevels = 0;
		
		for (int i=0; i<numItems; i++) {

			// -----------------------------------------
			// read the tuple section
			// -----------------------------------------

			short length = buffer.getShort();			
			assert(length>0);

			IdTuple item = new IdTuple(i, length);
			numLevels = Math.max(numLevels, item.length);
			
			for (int j=0; j<item.length; j++) {
				short kindInterpret = buffer.getShort();
				item.setKindAndInterpret(kindInterpret, j);
				item.physical_index[j] = buffer.getLong();
				item.logical_index[j]  = buffer.getLong();
				
				if (i==0)
					continue;
				// we don't care with summary profile
				
				if (mapLevelToHash[j] == null)
					mapLevelToHash[j] = new HashMap<Long, Integer>();
				
				// compute the number of appearances of a given kind and level
				// this is important to know if there's invariant or not
				Long hash = convertIdTupleToHash(j, item.getKind(j), item.physical_index[j]);
				Integer count = mapLevelToHash[j].get(hash);
				if (count == null) {
					count = Integer.valueOf(0);
				}
				count++;
				mapLevelToHash[j].put(hash, count);
				
				// find min and max for each level
				minIndex[j] = Math.min(minIndex[j], item.physical_index[j]);
				maxIndex[j] = Math.min(maxIndex[j], item.physical_index[j]);
				
				if (!hasGPU)
					hasGPU = item.isGPU(idTupleTypes);
			}
			if (i> 0) {
				listIdTuple.add(item);
			}
		}
		
		// -----------------------------------------
		// 2. Check for the invariants in id tuple.
		//    This is to know which the first levels we can skip
		// -----------------------------------------
		
		Map<Integer, Integer> mapLevelToSkip = new HashMap<Integer, Integer>();
		
		for(int i=0; i<mapLevelToHash.length; i++) {
			
			// find which levels we have to skip
			if (mapLevelToHash[i] != null && mapLevelToHash[i].size()==1) {
				// this level only has one variant.
				// we can skip it.
				mapLevelToSkip.put(i, 1);
				
			} else if (mapLevelToSkip.size()>0) {
				// if we find that this level is not invariant, we just stop here.
				// there is no need to continue to look for invariant.
				// for instance if we have:
				//   rank 0 thread 0
				//   rank 0 thread 1
				//   rank 0 stream 1 context 0
				// we just stop at level rank (rank 0), we don't need to skip level 2 (context 0)
				break;
			}
		}

		// -----------------------------------------
		// 2. sort the id tuple
		// -----------------------------------------
		
		listIdTuple.sort(new Comparator<IdTuple>() {

			@Override
			public int compare(IdTuple o1, IdTuple o2) {
				return o1.compareTo(o2);
			}
		});
		
		mapProfileToOrder = new HashMap<Integer, Integer>(listIdTuple.size());
		
		// -----------------------------------------
		// 3. a. compute the brief short version of id tuples
		//    b. store the order of sorted id tuple into a map
		// -----------------------------------------
		
		for(int i=0; i<listIdTuple.size(); i++) {
			IdTuple idt = listIdTuple.get(i);
			int totLevels = 0;
			
			// find how many levels we can keep for this id tuple
			for (int j=0; j<idt.length; j++) {
				if (mapLevelToSkip.get(j) == null) {
					totLevels++;
				}
			}
			// the profileNum is +1 because the index 0 is for summary
			IdTuple shortVersion = new IdTuple(idt.profileNum, totLevels);
			
			// this is a hack since hpcprof2-mpi doesn't generate sorted id tuple:
			// store the map from the profile index to the order index
			
			mapProfileToOrder.put(idt.profileNum, i);
			
			int level = 0;
			
			// copy not-skipped id tuples to the short version
			// leave the skipped ones for the full complete id tuple
			for(int j=0; j<idt.length; j++) {
				if (mapLevelToSkip.get(j) == null) {
					// we should keep this level
					short kind = idt.getKind(j);
					shortVersion.setKindAndInterpret(kind, level);
					shortVersion.physical_index[level] = idt.physical_index[j];
					level++;
				}
			}
			listIdTupleShort.add(shortVersion);
			
			numLevels = Math.max(numLevels, idt.length);
		}
	}

	
	/****
	 * Return {@code true} if the database contains GPU parallelism
	 * @return {@code boolean}
	 */
	public boolean hasGPU() {
		return hasGPU;
	}
	
	public IdTupleType getIdTupleType() {
		return idTupleTypes;
	}
	
	/****
	 * Retrieve the number of detected parallelism levels of this database
	 * @return {@code int}
	 */
	public int getParallelismLevels() {
		return numLevels;
	}
	
	/***
	 * Serialize id tuple into one long number.
	 * TODO: This is not a perfect serialization (max is 64 bits), 
	 * but it works for small number of profiles
	 * 
	 * @param level
	 * @param kind
	 * @param index
	 * @return
	 */
	private long convertIdTupleToHash(int level, int kind, long index) {
		long k = (kind << 20);
		long t = k + index;
		return t;
	}
	
	
	/*****
	 * read the list of Prof Info
	 * @param input FileChannel
	 * @throws IOException
	 */
	private void readProfInfo(FileChannel input, DataSection profSection) throws IOException {
		
		input.position(profSection.offset);

		listProfInfo = new ArrayList<DataSummary.ProfInfo>((int) numItems);

		long position_profInfo = input.position();
		long profInfoSize = numItems * ProfInfo.SIZE;
		
		MappedByteBuffer mappedBuffer = input.map(MapMode.READ_ONLY, position_profInfo, profInfoSize);

		for(int i=0; i<numItems; i++) {
			ProfInfo info = new ProfInfo();
			info.id_tuple_ptr = mappedBuffer.getLong();
			info.metadata_ptr = mappedBuffer.getLong();
			mappedBuffer.getLong(); // spare 1
			mappedBuffer.getLong(); // spare 2
			info.num_vals = mappedBuffer.getLong();
			info.num_nz_contexts = mappedBuffer.getInt();
			info.offset = mappedBuffer.getLong();
			
			listProfInfo.add(info);
		}
		// make sure the next reader have pointer to the last read position
		
		position_profInfo += profInfoSize;
		input.position(position_profInfo);
	}
	
	
	/***
	 * Binary search the cct index 
	 * 
	 * @param index the cct index
	 * @param first the beginning of the relative index
	 * @param last  the last of the relative index
	 * @param buffer ByteBuffer of the file
	 * @return 2-length array of indexes: the index of the found cct, and its next index
	 */
	private long[] binarySearch(int index, int first, int last, ByteBuffer buffer) {
		int begin = first;
		int end   = last;
		int mid   = (begin+end)/2;

		while (begin <= end) {
			buffer.position(mid * CCT_RECORD_SIZE);
			int cctidx  = buffer.getInt();
			long offset = buffer.getLong();
			
			if (cctidx < index) {
				begin = mid+1;
			} else if(cctidx == index) {
				long nextIndex = offset;
				
				if (mid+1<last) {
					buffer.position(CCT_RECORD_SIZE * (mid+1));
					buffer.getInt();
					nextIndex = buffer.getLong();
				}
				return new long[] {offset, nextIndex};
			} else {
				end = mid-1;
			}
			mid = (begin+end)/2;
		}
		// not found
		return null;
	}
	
	/***
	 * Get the CCT index record from a given index position
	 * @param buffer byte buffer
	 * @param position int index of the cct
	 * @return int
	 */
	private int getCCTIndex(ByteBuffer buffer, int position) {
		final int adjustedPosition = position * CCT_RECORD_SIZE;
		buffer.position(adjustedPosition);
		return buffer.getInt();
	}
	
	/****
	 * Get the file offset of a CCT. 
	 * @param buffer ByteBuffer of the file
	 * @param position int index of the cct
	 * @return long
	 */
	private long getCCTOffset(ByteBuffer buffer, int position) {
		buffer.position( (position * CCT_RECORD_SIZE) + Integer.BYTES);
		return buffer.getLong();
	}
	
	/***
	 * Newton-style of Binary search the cct index 
	 * 
	 * @param cct the cct index
	 * @param first the beginning of the relative index
	 * @param last  the last of the relative index
	 * @param buffer ByteBuffer of the file
	 * @return 2-length array of indexes: the index of the found cct, and its next index
	 */
	private long[] newtonSearch(int cct, int first, int last, ByteBuffer buffer) {
		int left_index  = first;
		int right_index = last - 1;
		
		int left_cct  = getCCTIndex(buffer, left_index);
		int right_cct = getCCTIndex(buffer, right_index);
		
		while (right_index - left_index > 1) {
			
			int predicted_index;
			final float cct_range = right_cct - left_cct;
			final float rate = cct_range / (right_index - left_index);
			final int mid_cct = (int) cct_range / 2;
			
			if (cct <= mid_cct) {
				predicted_index = (int) Math.max( ((cct - left_cct)/rate) + left_index, left_index);
			} else {
				predicted_index = (int) Math.min(right_index - ((right_cct-cct)/rate), right_index);
			}
			
			if (predicted_index <= left_cct) {
				predicted_index = left_index + 1;
			} 
			if (predicted_index >= right_cct) {
				predicted_index = right_index - 1;
			}
			
			int current_cct = getCCTIndex(buffer, predicted_index);
			
			if (cct >= current_cct) {
				left_index = predicted_index;
				left_cct = current_cct;
			} else {
				right_index = predicted_index;
				right_cct = current_cct;
			}
		}
		
		boolean found = cct == left_cct || cct == right_cct;
		
		if (found) {
			int index = left_index;
			if (cct == right_cct) 
				// corrupt data: should throw exception 
				index = right_index;
			
			long o1 = getCCTOffset(buffer, index);
			long o2 = getCCTOffset(buffer, index + 1);

			return new long[] {o1, o2};
		}
		// not found: the cct node has no metrics. 
		// we may should return array of zeros instead of null
		return null;
	}
	

	/*******
	 * print a list of metrics for a given CCT index
	 * 
	 * @param out : the outpur stream
	 * @param cct : CCT index
	 */
	private void printMetrics(PrintStream out, int cct)
	{
		try {
			List<MetricValueSparse> values = getMetrics(cct);
			if (values == null)
				return;
			
			for(MetricValueSparse value: values) {
				System.out.print(value.getIndex() + ": " + value.getValue() + " , ");
			}
			System.out.println();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			//out.println();
		}
	}

	
	// --------------------------------------------------------------------
	// Internal Classes
	// --------------------------------------------------------------------

	public static class ListCCTAndIndex
	{
		public int  []listOfId;
		public long []listOfdIndex;
		
		public String toString() {
			String buffer = "";
			if (listOfId != null) {
				buffer += "id: [" + listOfId[0] + ".." + listOfId[listOfId.length-1] + "] ";
			}
			if (listOfdIndex != null) {
				buffer += "idx; [" + listOfdIndex[0] + ".." + listOfdIndex[listOfdIndex.length-1] + "]";
			}
			return buffer;
		}
	}
	
	/*****
	 * 
	 * Prof info data structure
	 *
	 */
	protected static class ProfInfo
	{
		/** the size of the record in bytes  */
		public static final int SIZE = 8 + 8 + 8 + 8 + 8 + 4 + 8;
		
		public long id_tuple_ptr;
		public long metadata_ptr;
		public long num_vals;
		public int  num_nz_contexts;
		public long offset;
		
		public String toString() {
			return "tuple_ptr: " + id_tuple_ptr    + 
				   ", vals: " 	 + num_vals 	   + 
				   ", ccts: "	 + num_nz_contexts + 
				   ", offs: " 	 + offset;
		}
	}

	/***************************
	 * unit test 
	 * 
	 * @param argv
	 ***************************/
	public static void main(String []argv)
	{
		final String DEFAULT_FILE = "/Users/la5/data/sparse/hpctoolkit-database/thread.db";
		final String filename;
		if (argv != null && argv.length>0)
			filename = argv[0];
		else
			filename = DEFAULT_FILE;
		IdTupleType type = IdTupleType.createTypeWithOldFormat();
		
		final DataSummary summary_data = new DataSummary(type);
		try {
			summary_data.open(filename);			
			summary_data.printInfo(System.out);
			summary_data.dispose();	
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
