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
	
	private int  []kind;
	public long  []physical_index;
	public long  []logical_index;

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
		
		kind  = new int[length];
		physical_index = new long[length];
		logical_index  = new long[length];
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

	/***
	 * Get the interpret version of the id tuple for a given level
	 * @param level
	 * @return {@code short}
	 */
	public short getInterpret(int level) {
		return (short) (((kind[level])>>14) & 0x3);
	}
	
	
	/****
	 * Get the kind of this id-tuple
	 * @param level
	 * @return
	 */
	public short getKind(int level) {
		return (short) ((kind[level]) & ((1<<14)-1));
	}
	
	/***
	 * Store the kind-and-interpret value for a given level
	 * @param kindAndInterpet a combined value of kind and interpret
	 * @param level
	 */
	public void setKindAndInterpret(int kindAndInterpet, int level) {
		kind[level] = kindAndInterpet;
	}
	
	/****
	 * Get the index in this id tuple for a certain type.
	 * Example: If id tuple is (rank 0, thread 1), and calling:
	 * <pre>
	 * getIndex(IdTupleType.KIND_RANK) will return 0
	 * getIndex(IdTupleType.KIND_THREAD) returns 1
	 * </pre>
	 * @param kindRank {@code short} the type of id tuple. Must be one of IdTupleType.KIND_*
	 * 
	 * @return {@code long} the index of the id tuple. Zero if no type is found.
	 */
	public long getIndex(int kindRank) {
		for (int i=0; i<length; i++) {
			if (kind[i] == kindRank) {
				return physical_index[i];
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
			
			diff = (int) (physical_index[i] - another.physical_index[i]);
			if (diff != 0)
				return diff;
			
			diff = (int) (logical_index[i] - another.logical_index[i]);
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
			if (getKind(i) == kindType)
				return true;
		}
		return false;
	}
	
	
	public boolean hasKind(String prefix, IdTupleType type) {
		for(short i=0; i<kind.length; i++) {
			if (type.kindStr(kind[i]).startsWith(prefix))
				return true;
		}
		return false;
	}
	
	
	public boolean isGPU(int level, IdTupleType type) {
		int kindType = IdTupleType.getKind(kind[level]);
		return (type.getLabel(kindType).startsWith(IdTupleType.PREFIX_GPU));
	}
	
	
	public boolean isGPU(IdTupleType type) {
		for (int i=0; i<kind.length; i++) {
			if (isGPU(i, type))
				return true;
		}
		return false;
	}
	
	
	public String toString(IdTupleType idTupleType) {
		return toString(kind.length-1, idTupleType);
	}

	
	
	/***
	 * Returns the string representation of this object.
	 * @return String
	 */
	public String toString(int level, IdTupleType idTupleType) {
		String buff = "";
		if (kind != null && physical_index != null)
			for(int i=0; i<=level; i++) {
				if (i>0)
					buff += " ";
				buff += idTupleType.kindStr(kind[i]) + " " ;

				switch (IdTupleType.getInterpret(kind[i])) {
				case IdTupleType.IDTUPLE_IDS_BOTH_VALID:
					// physical and logical
					if (idTupleType.getMode() == IdTupleType.Mode.LOGICAL) {
						buff += logical_index[i];
					} else {
						buff += physical_index[i];
					}
					break;
				case IdTupleType.IDTUPLE_IDS_LOGIC_ONLY:
					// logical only
					buff += logical_index[i];
					break;
				case IdTupleType.IDTUPLE_IDS_LOGIC_GLOBAL:
					// physical
				case IdTupleType.IDTUPLE_IDS_LOGIC_LOCAL:
					buff += physical_index[i] + "*";
					break;
					// physical
				}
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
		
		if (kind != null && physical_index != null && level>=0 && level<length) {
			String str = "";
			
			for(int i=0; i<=level; i++) {
				
				long lblIndex = physical_index[i]; 
				if (i==1) {
					str += ".";
				} else if (i>1) {
					lblIndex = (long) (Math.pow(10, level-i) * physical_index[i]);
				}
				str += lblIndex;
			}
			return str;
		}
		return STRING_EMPTY;
	}
}
