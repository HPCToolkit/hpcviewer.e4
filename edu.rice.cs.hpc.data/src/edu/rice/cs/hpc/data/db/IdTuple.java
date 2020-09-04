package edu.rice.cs.hpc.data.db;

public class IdTuple 
{
	// -------------------------------------------
	// constants
	// -------------------------------------------
	
	public final static int TUPLE_LENGTH_SIZE = 2;
	public final static int TUPLE_KIND_SIZE   = 2;
	public final static int TUPLE_INDEX_SIZE  = 8;
	
	
	public final static int KIND_SUMMARY = 0;
	public final static int KIND_RANK    = 1;
	public final static int KIND_THREAD  = 2;
	
	public final static String KIND_LABEL_SUMMARY = "Summary";
	public final static String KIND_LABEL_RANK    = "Rank";
	public final static String KIND_LABEL_THREAD  = "Thread";
	
	private final static String[] arrayLabel = {KIND_LABEL_SUMMARY, 
												KIND_LABEL_RANK,
												KIND_LABEL_THREAD};

	// -------------------------------------------
	// variables
	// -------------------------------------------

	public int length;
	short []kind;
	long  []index;
	
	
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
	 * Returns the string representatin of this object.
	 * @return String
	 */
	public String toString() {
		String buff = "len: " + length;
		if (kind != null && index != null)
			for(int i=0; i<kind.length; i++) {
				buff += " (" + kindStr(kind[i]) + " " + index[i] + ")";
			}
		return buff;
	}

}
