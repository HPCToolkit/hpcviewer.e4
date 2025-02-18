// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.data.util;

public class Constants {
	
	/**The min number of time units you can zoom in*/
	public static final int MIN_TIME_UNITS_DISP = 1;
	
	public static final int dataIdxNULL = -1;
	public static final int dataIdxNoData = 0; // corresponds to "no data"
	public static final String dataStrNULL = null;
	
	public static final int DONE = 0x444F4E45;
	public static final int OPEN = 0x4F50454E;
	public static final int INFO = 0x494E464F;
	public static final int XML_HEADER = 0x45584D4C;
	public static final int DATABASE_NOT_FOUND = 0x4E4F4442;
	public static final int DB_OK = 0x44424F4B;
}
