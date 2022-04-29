package edu.rice.cs.hpcdata.util;

public class Constants {

	//-----------------------------------------------------------
	// CONSTANTS
	//-----------------------------------------------------------
	
	public static final int MULTI_PROCESSES = 1;
	public static final int MULTI_THREADING = 2;

	public static final int SIZEOF_LONG  = Long.SIZE / Byte.SIZE;
	public static final int SIZEOF_INT 	 = Integer.SIZE / Byte.SIZE;
	public static final int SIZEOF_FLOAT = Float.SIZE / Byte.SIZE;
	
	static public final int TOOLTIP_DELAY_MS  = 2000;
	
	public final static int EXPERIMENT_SPARSE_VERSION = 4;
	public final static int EXPERIMENT_DENSED_VERSION = 2;
	
	public final static String PROCEDURE_UNKNOWN = "<unknown>";
	
	public static final String  PROC_NO_ACTIVITY   = "<no activity>";
	public static final String  PROC_NO_THREAD     = "<no thread>";

	//
	// Flat id for certain scopes are reserved:
	//  0 : for the root
	//  1 : reserved
	//  2 : <module unknown>
	//  3 : <file unknown>
	//  4 : <procedure unknown> or simply <unknown>
	//  5-9: reserved
	//
	public static final int FLAT_ID_LOAD_UNKNOWN   = 2;
	public static final int FLAT_ID_FILE_UNKNOWN   = 3;
	public static final int FLAT_ID_PROC_UNKNOWN   = 4;
	public static final int FLAT_ID_BEGIN          = 10;
}
