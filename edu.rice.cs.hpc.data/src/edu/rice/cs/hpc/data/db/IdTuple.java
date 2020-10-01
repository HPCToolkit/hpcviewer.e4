package edu.rice.cs.hpc.data.db;

/*******************************************
 * 
 * Id Tuple class to store the length, kind and index of the id tuple.
 * <p>Id tuple contains the information of a measurement profile. 
 * The later is equal to *.hpcrun file, plus *.hpctrace file.
 *
 *******************************************/
public class IdTuple 
{
	// -------------------------------------------
	// constants
	// -------------------------------------------
	
	public final static int TUPLE_LENGTH_SIZE = 2;
	public final static int TUPLE_KIND_SIZE   = 2;
	public final static int TUPLE_INDEX_SIZE  = 8;
	
	// use for backward compatibility
	// we will convert old database process.thread format
	// to id-tuples
	
	public final static int KIND_SUMMARY = 0;
	public final static int KIND_NODE    = 1;
	public final static int KIND_RANK    = 2;
	public final static int KIND_THREAD  = 3;
	
	public final static int KIND_GPU_DEVICE  = 4;
	public final static int KIND_GPU_STREAM  = 5;
	public final static int KIND_GPU_CONTEXT = 6;
	public final static int KIND_CORE        = 7;
	
	public final static int KIND_MAX         = 8;
	
	
	// see https://github.com/HPCToolkit/hpctoolkit/blob/prof2/src/lib/prof-lean/id-tuple.h#L81
	// for list of kinds in id tuple
	
	private final static String KIND_LABEL_SUMMARY = "Summary";
	private final static String KIND_LABEL_NODE    = "Node";
	private final static String KIND_LABEL_RANK    = "Rank";
	private final static String KIND_LABEL_THREAD  = "Thread";
	
	private final static String KIND_LABEL_GPU_DEVICE = "Device";
	private final static String KIND_LABEL_GPU_STREAM = "Stream";
	private final static String KIND_LABEL_GPU_CTXT   = "Context";
	
	private final static String KIND_LABEL_CORE       = "Core";

	private final static String[] arrayLabel = {KIND_LABEL_SUMMARY, 
											    KIND_LABEL_NODE,
												KIND_LABEL_RANK,
												KIND_LABEL_THREAD,
												KIND_LABEL_GPU_DEVICE,
												KIND_LABEL_GPU_STREAM,
												KIND_LABEL_GPU_CTXT,
												KIND_LABEL_CORE};

	// -------------------------------------------
	// variables
	// -------------------------------------------

	public int   length;
	public short []kind;
	public long  []index;
	

	// -------------------------------------------
	// Constructors
	// -------------------------------------------
	
	/****
	 * 
	 * @param length
	 */
	public IdTuple(int length) {
		this.length = length;
		
		kind  = new short[length];
		index = new long[length];
	}
	
	public IdTuple() {
		length = 0;
	}
	

	// -------------------------------------------
	// API Methods
	// -------------------------------------------
	
	/***
	 * Check if this tuple has a specific kind.
	 * @see KIND_NODE
	 * @see KIND_THREAD
	 * @see KIND_GPU_CONTEXT
	 * 
	 * @param kindType short
	 * @return boolean true if the tuple has the kind type, false otherwise
	 */
	public boolean hasKind(short kindType) {
		for(short i=0; i<kind.length; i++) {
			if (kind[i] == kindType)
				return true;
		}
		return false;
	}
	
	/***
	 * Conversion from a tuple kind to label string
	 * @param kind
	 * @return String label of a kind 
	 * @exception java.lang.ArrayIndexOutOfBoundsException if the kind is invalid
	 */
	public String kindStr(short kind)
	{
		assert(kind>=0 && kind<arrayLabel.length);
		
		return arrayLabel[kind];
	}
	
	
	/***
	 * Returns the string representation of this object.
	 * @return String
	 */
	public String toString() {
		return toString(kind.length-1);
	}
	
	/***
	 * Returns the string representation of this object.
	 * @return String
	 */
	public String toString(int level) {
		String buff = "";
		if (kind != null && index != null)
			for(int i=0; i<=level; i++) {
				if (i>0)
					buff += " ";
				
				buff += kindStr(kind[i]) + " " + index[i];
			}
		return buff;
	}


	
	/****
	 * get the number representation of the id tuple.
	 * If the id tuple has length 2, its label will be X.Y
	 * 
	 * @return the number representation of id tuple
	 */
	public double toNumber() {
		Double number = 0.0;
		
		String str = toLabel();
		try {
			number = Double.valueOf(str);
		} catch (NumberFormatException e) {
			// Can't convert to number. The length must be bigger than 2
		}
		return number;
	}
	
	
	/****
	 * Retrieve the string label of the id tuple for all levels.
	 * @return String
	 */
	public String toLabel() {
		return toLabel(length-1);
	}
	
	
	/****
	 * Retrieve the string label of the id tuple for a certain level.
	 * If the id tuple has 4 levels, and user specifies 2, then it returns the first 2 levels.
	 * 
	 * @param level int
	 * @return
	 */
	public String toLabel(int level) {
		
		if (kind != null && index != null && level>=0 && level<length) {
			String str = "";
			
			for(int i=0; i<=level; i++) {
				if (i==1) {
					str += ".";
				}
				str += index[i];
			}
			return str;
		}
		return null;
	}
}
