package edu.rice.cs.hpcdata.util;

public class Constants {

	//-----------------------------------------------------------
	// CONSTANTS
	//-----------------------------------------------------------
	
	static public final int MULTI_PROCESSES = 1;
	static public final int MULTI_THREADING = 2;

	public static final int SIZEOF_LONG  = Long.SIZE / Byte.SIZE;
	public static final int SIZEOF_INT 	 = Integer.SIZE / Byte.SIZE;
	public static final int SIZEOF_FLOAT = Float.SIZE / Byte.SIZE;

	static public final String DATABASE_FILENAME = "experiment.xml";
	
	static public final int    TOOLTIP_DELAY_MS  = 2000;
	
	public final static int EXPERIMENT_SPARSE_VERSION = 4;
	public final static int EXPERIMENT_DENSED_VERSION = 2;
}
