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
	
	public static final int TUPLE_LENGTH_SIZE = 2;
	public static final int TUPLE_KIND_SIZE   = 2;
	public static final int TUPLE_INDEX_SIZE  = 8;
	
	private static final String STRING_EMPTY = null;
	private static final String SEPARATOR = " ";
	private static final String SPACE = " ";

	/***
	 * Special id-tuple for summary profile, which is the profile
	 * of the whole execution of the application. 
	 */
	public static final IdTuple PROFILE_SUMMARY = new IdTuple(0, 0);
	
	// -------------------------------------------
	// variables
	// -------------------------------------------

	private final int  profileIndex;
	
	private byte []kinds;
	private byte []flags;
	private long []physicalIndexes;
	private int  []logicalIndexes;

	// -------------------------------------------
	// Constructors
	// -------------------------------------------
	
	/****
	 * Constructor
	 * Caller needs to set {@code kind} and {@code index} variables
	 * 
	 * @param index
	 * 			id or index of this profile. It has to be unique.
	 * @param length 
	 * 			the int the length (or level) of this id tuple
	 */
	public IdTuple(int index, int length) {		
		kinds  = new byte[length];
		flags  = new byte[length];
		physicalIndexes = new long[length];
		logicalIndexes  = new int [length];
		
		this.profileIndex = index;
	}
	

	// -------------------------------------------
	// API Methods
	// -------------------------------------------
	
	public int getProfileIndex() {
		return profileIndex;
	}
	
	@Override
	public boolean equals(Object idt) {
		if (!(idt instanceof IdTuple))
			return false;
		
		return compareTo((IdTuple) idt) == 0;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	/****
	 * Get the index in this id tuple for a certain type.
	 * Example: If id tuple is (rank 0, thread 1), and calling:
	 * <pre>
	 * getIndex(IdTupleType.KIND_RANK) will return 0
	 * getIndex(IdTupleType.KIND_THREAD) returns 1
	 * </pre>
	 * @param kindType {@code short} the type of id tuple. Must be one of IdTupleType.KIND_*
	 * 
	 * @return {@code long} the index of the id tuple. 
	 * 			A negative number if no type is found.
	 */
	public long getIndex(byte kindType) {
		for (int i=0; i<kinds.length; i++) {
			if (kinds[i] == kindType) {
				return physicalIndexes[i];
			}
		}
		return -1;
	}
	
	public int getLength() {
		return kinds.length;
	}

	
	/****
	 * Get the kind of this id-tuple
	 * @param level
	 * @return
	 */
	public byte getKind(int level) {
		return kinds[level];
	}

	public void setKind(int index, byte kind) {
		this.kinds[index] = kind;
	}
	
	public byte getFlag(int index) {
		return flags[index];
	}
	
	public void setFlag(int index, byte flag) {
		this.flags[index] = flag;
	}

	public long getPhysicalIndex(int index) {
		return physicalIndexes[index];
	}

	public void setPhysicalIndex(int index, long physical_index) {
		this.physicalIndexes[index] = physical_index;
	}

	public int getLogicalIndex(int index) {
		return logicalIndexes[index];
	}

	public void setLogicalIndex(int index, int logical_index) {
		this.logicalIndexes[index] = logical_index;
	}

	/****
	 * Compare this id-tuple with another id-tuple.
	 * The comparison includes: the length, the kind, physical and logical indexes.
	 * <p>
	 * This method returns:
	 * <ul>
	 *  <li> Zero if the both are exactly the same
	 *  <li> Negative number if this id-tuple is lexicographically less than the other tuple
	 *  <li> Positive number if this id-tuple is lexicographically bigger
	 * </ul>
	 * 
	 * @param another
	 * 			The other id-tuple to compare
	 * @return {@code int} 
	 */
	public int compareTo(IdTuple another) {
		int minLength = Math.min(kinds.length, another.kinds.length);
		
		for(int i=0; i<minLength; i++) {
			// hack - hack -hack
			// Fix issue #254 no sort on host node 
			if (kinds[i] == IdTupleType.KIND_NODE)
				continue;
			
			// compare the differences in kind. 
			// If they are different, we stop here
			// another we compare the difference in index
			
			int diff = kinds[i] - another.kinds[i];
			if (diff != 0)
				return diff;
			
			diff = (int) (physicalIndexes[i] - another.physicalIndexes[i]);
			if (diff != 0)
				return diff;
			
			diff = logicalIndexes[i] - another.logicalIndexes[i];
			if (diff != 0)
				return diff;
		}
		return kinds.length-another.kinds.length;
	}
		
	
	public boolean isGPU(int level, IdTupleType type) {
		byte kindType = kinds[level];
		String kindStr = type.getLabel(kindType);
		return (kindStr.startsWith(IdTupleType.PREFIX_GPU));
	}
	
	
	public boolean isGPU(IdTupleType type) {
		for (int i=0; i<kinds.length; i++) {
			if (isGPU(i, type))
				return true;
		}
		return false;
	}
	
	
	@Override
	public String toString() {
		if (kinds == null || physicalIndexes == null)
			return "";
		
		var sb = new StringBuilder();
		for(var pi: physicalIndexes) {
			sb.append(pi);
			sb.append(" ");
		}
		return sb.toString();
	}
	
	public String toString(IdTupleType idTupleType) {
		return toString(kinds.length-1, idTupleType);
	}

	
	
	/***
	 * Returns the string representation of this object.
	 * @return String
	 */
	public String toString(int level, IdTupleType idTupleType) {
		if (kinds == null || physicalIndexes == null)
			return null;
		
		StringBuilder buff = new StringBuilder();
		appendStringIdTuple(buff, 0, idTupleType);
		
		for(int i=1; i<=level; i++) {
			buff.append(SEPARATOR);
			appendStringIdTuple(buff, i, idTupleType);
		}
		return buff.toString();
	}

	
	private void appendStringIdTuple(StringBuilder buff, int level, IdTupleType idTupleType) {
		String kindStr = idTupleType.kindStr(kinds[level]); 
		buff.append(kindStr);
		buff.append(SPACE);
		buff.append(getIndexBaseOnFlag(level));
	}
	
	
	private long getIndexBaseOnFlag(int level) {
		if ((flags[level] & 0x1) == 0x1) 
			return physicalIndexes[level];
		return logicalIndexes[level];
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
		return toLabel(kinds.length-1);
	}
	
	
	/****
	 * Retrieve the string label of the id tuple for a certain level.
	 * If the id tuple has 4 levels, and user specifies 2, then it returns the first 2 levels.
	 * 
	 * @param level int
	 * @return
	 */
	public String toLabel(int level) {
		
		if (kinds != null && logicalIndexes != null && level>=0 && level<kinds.length) {
			String str = "";
			
			for(int i=0; i<=level; i++) {
				
				long lblIndex = logicalIndexes[i]; 
				if (i==1) {
					str += ".";
				} else if (i>1) {
					lblIndex = (long) (Math.pow(10, (double)level-i) * logicalIndexes[i]);
				}
				str += lblIndex;
			}
			return str;
		}
		return STRING_EMPTY;
	}
}
