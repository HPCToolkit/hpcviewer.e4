package edu.rice.cs.hpcdata.db;

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
	
	private final static String STRING_EMPTY = null;

	// -------------------------------------------
	// variables
	// -------------------------------------------

	public int   profileNum;
	public int   length;
	
	public short []kind;
	public long  []index;
	

	// -------------------------------------------
	// Constructors
	// -------------------------------------------
	
	/****
	 * Constructor
	 * Caller needs to set {@code kind} and {@code index} variables
	 * @param profileNum int the profile index
	 * @param length the int the length (or level) of this id tuple
	 */
	public IdTuple(int profileNum, int length) {
		this.length     = length;
		this.profileNum = profileNum;
		
		kind  = new short[length];
		index = new long[length];
	}
	
	/***
	 * Empty constructor.
	 * Caller needs to set {@code profileNum}, {@code length}, {@code kind} and {@code index} variables
	 */
	public IdTuple() {
		length = 0;
		profileNum = 0;
	}
	

	// -------------------------------------------
	// API Methods
	// -------------------------------------------

	/****
	 * Get the index in this id tuple for a certain type.
	 * Example: If id tuple is (rank 0, thread 1), and calling:
	 * <pre>
	 * getIndex(IdTupleType.KIND_RANK) will return 0
	 * getIndex(IdTupleType.KIND_THREAD) returns 1
	 * </pre>
	 * @param type {@code short} the type of id tuple. Must be one of IdTupleType.KIND_*
	 * 
	 * @return {@code long} the index of the id tuple. Zero if no type is found.
	 */
	public long getIndex(short type) {
		for (int i=0; i<length; i++) {
			if (kind[i] == type) {
				return index[i];
			}
		}
		return 0;
	}
	
	public int compareTo(IdTuple another) {
		int minLength = Math.min(length, another.length);
		
		for(int i=0; i<minLength; i++) {
			// compare the differences in kind. 
			// If they are different, we stop here
			// another we compare the difference in index
			
			int diff = kind[i] - another.kind[i];
			if (diff != 0)
				return diff;
			
			diff = (int) (index[i] - another.index[i]);
			if (diff != 0)
				return diff;
		}
		return length-another.length;
	}
	
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
				String strIndex = String.valueOf(index[i]);
				
				if (kind[i] == IdTupleType.KIND_NODE) {
					strIndex = Long.toHexString(index[i]);
				}
				buff += IdTupleType.kindStr(kind[i]) + " " + strIndex;
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
				if (kind[i] == IdTupleType.KIND_RANK   || 
					kind[i] == IdTupleType.KIND_THREAD ||
					kind[i] == IdTupleType.KIND_GPU_STREAM ||
					kind[i] == IdTupleType.KIND_GPU_CONTEXT) {
					
					long lblIndex = index[i]; 
					if (i==1) {
						str += ".";
					} else if (i>1) {
						lblIndex = (long) (Math.pow(10, level-i) * index[i]);
					}
					str += lblIndex;
				}
			}
			return str;
		}
		return STRING_EMPTY;
	}
}
