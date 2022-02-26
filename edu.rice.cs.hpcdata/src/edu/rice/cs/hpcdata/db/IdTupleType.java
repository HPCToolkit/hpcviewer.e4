package edu.rice.cs.hpcdata.db;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/***********************************
 * 
 * Type definition of id-tuples
 *
 * 	see about id-tuple type info in hpctoolkit repository:
 * <code>
	https://github.com/HPCToolkit/hpctoolkit/blob/prof2/src/lib/prof-lean/id-tuple.h#L81
	https://github.com/HPCToolkit/hpctoolkit/blob/prof2/doc/FORMATS.md
 * </code>	
 ***********************************/
public class IdTupleType 
{
	public static enum Mode {LOGICAL, PHYSICAL};
	
	public final static String PREFIX_GPU = "GPU";
	
	// use for backward compatibility
	// we will convert old database process.thread format
	// to id-tuples
	
	public final static byte KIND_SUMMARY = 0;
	public final static byte KIND_NODE    = 1;
	public final static byte KIND_RANK    = 2;
	public final static byte KIND_THREAD  = 3;
	public final static byte KIND_GPUDEVICE  = 4;
	public final static byte KIND_GPUCONTEXT = 5;
	public final static byte KIND_GPUSTREAM  = 6;
	public final static byte KIND_CORE       = 7;
	
	public final static String LABEL_SUMMARY    = "Summary";
	public final static String LABEL_NODE       = "Node";
	public final static String LABEL_RANK       = "Rank";
	public final static String LABEL_THREAD     = "Thread";
	public final static String LABEL_GPUDEVICE  = "GPUDevice";
	public final static String LABEL_GPUCONTEXT = "GPUContext";
	public final static String LABEL_GPUSTREAM  = "GPUStream";
	public final static String LABEL_CORE		= "Core";
	
	// Constants copied from 
	// https://github.com/HPCToolkit/hpctoolkit/blob/aa60b422e18f300a0d1ac4f5e365e98e37d45c8a/src/lib/prof-lean/id-tuple.h#L103-L110

	/**
	 * BOTH_VALID: Both logical and physical IDs are presentable. For Viewer present logical ID by default and allow physical on request
	 */
	public final static int IDTUPLE_IDS_BOTH_VALID   = 0;
	/**
	 * LOGICAL_LOCAL: For Prof2: logical ID should be generated based on shared prefix. 
	 * For Viewer present physical ID as if it was logical (and warn that something went wrong in Prof2).
	 */
	public final static int IDTUPLE_IDS_LOGIC_LOCAL  = 1;
	/**
	 * LOGICAL_GLOBAL: For Prof2: logical ID should be generated based on single tuple. For Viewer same as LOGICAL_LOCAL.
	 */
	public final static int IDTUPLE_IDS_LOGIC_GLOBAL = 2;
	/**
	 * LOGICAL_ONLY: Disregard physical ID, only logical ID is presentable. For Viewer present logical ID and never present physical
	 */
	public final static int IDTUPLE_IDS_LOGIC_ONLY   = 3;


	private final Map<Byte, String> mapIdTuple = new HashMap<>();
	private Mode mode = Mode.PHYSICAL;
	
	
	public void initDefaultTypes() {
		mapIdTuple.put(KIND_SUMMARY,   LABEL_SUMMARY);
		mapIdTuple.put(KIND_NODE,   LABEL_NODE);
		mapIdTuple.put(KIND_RANK,   LABEL_RANK);
		mapIdTuple.put(KIND_THREAD, LABEL_THREAD);
		mapIdTuple.put(KIND_GPUDEVICE,   LABEL_GPUDEVICE);
		mapIdTuple.put(KIND_GPUCONTEXT, LABEL_GPUCONTEXT);
		mapIdTuple.put(KIND_GPUSTREAM,   LABEL_GPUSTREAM);
		mapIdTuple.put(KIND_CORE, LABEL_CORE);
	}
	
	/***
	 * Create id tuple type using the traditional old format
	 * @return {@code IdTupleType}
	 */
	public static IdTupleType createTypeWithOldFormat() {
		IdTupleType type = new IdTupleType();
		type.initDefaultTypes();
		return type;
	}
	
	
	/****
	 * Add a new id tuple type
	 * @param kind
	 * @param label
	 */
	public void add(byte kind, String label) {
		mapIdTuple.put(kind, label);
	}
	
	
	/***
	 * get the label of the id-tuple
	 * @param kind
	 * @return
	 */
	public String getLabel(byte kind) {
		return mapIdTuple.get(kind);
	}
	
	
	/***
	 * get the entry set of the id tuple types
	 * @return {@code Set<Entry<Integer, String>> }
	 */
	public Set<Entry<Byte, String>>  entrySet() {
		return mapIdTuple.entrySet();
	}
	
	/***
	 * Conversion from a tuple kind to label string
	 * @param kind
	 * @return String label of a kind 
	 * @exception java.lang.ArrayIndexOutOfBoundsException if the kind is invalid
	 */
	public String kindStr(byte kind)
	{
		assert(kind>=0 && mapIdTuple != null && mapIdTuple.containsKey(kind));
		
		return mapIdTuple.get(kind);
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

}
