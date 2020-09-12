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
	
	// see https://github.com/HPCToolkit/hpctoolkit/blob/prof2/src/lib/prof-lean/id-tuple.h#L81
	// for list of kinds in id tuple
	
	public final static String KIND_LABEL_SUMMARY = "Summary";
	public final static String KIND_LABEL_NODE    = "Node";
	public final static String KIND_LABEL_RANK    = "Rank";
	public final static String KIND_LABEL_THREAD  = "Thread";
	
	public final static String KIND_LABEL_GPU_DEVICE = "Device";
	public final static String KIND_LABEL_GPU_STREAM = "Stream";
	public final static String KIND_LABEL_GPU_CTXT   = "Context";
	
	private final static String[] arrayLabel = {KIND_LABEL_SUMMARY, 
											    KIND_LABEL_NODE,
												KIND_LABEL_RANK,
												KIND_LABEL_THREAD,
												KIND_LABEL_GPU_DEVICE,
												KIND_LABEL_GPU_STREAM,
												KIND_LABEL_GPU_CTXT};

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
	 * Returns the string representation of this object.
	 * @return String
	 */
	public String toString() {
		String buff = "";
		if (kind != null && index != null)
			buff += toLabel() + " ";
			for(int i=0; i<kind.length; i++) {
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
	public double toLabel() {
		double label = 0.0d;
		
		if (kind != null && index != null) {
			String str = "";
			
			// TODO: need to make sure the length is 2
			
			for(int i=0; i<kind.length; i++) {
				if (i==1) {
					str += ".";
				}
				str += index[i];
			}
			label = Double.valueOf(str);
		}
			
		return label;
	}
}
