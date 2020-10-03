package edu.rice.cs.hpc.data.db;

public class IdTupleType 
{
	
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

	public final static String[] kindLabels  = {KIND_LABEL_SUMMARY, 
											    KIND_LABEL_NODE,
												KIND_LABEL_RANK,
												KIND_LABEL_THREAD,
												KIND_LABEL_GPU_DEVICE,
												KIND_LABEL_GPU_STREAM,
												KIND_LABEL_GPU_CTXT,
												KIND_LABEL_CORE};

	
	/***
	 * Conversion from a tuple kind to label string
	 * @param kind
	 * @return String label of a kind 
	 * @exception java.lang.ArrayIndexOutOfBoundsException if the kind is invalid
	 */
	public static String kindStr(short kind)
	{
		assert(kind>=0 && kind<kindLabels.length);
		
		return kindLabels[kind];
	}

}
