// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpclocal;

import org.hpctoolkit.db.local.util.Constants;

class Header 
{
	public final static String Magic   = "HPCRUN-trace______";
	public final static int MagicLen   = 18;
	public final static int VersionLen = 5;
	public final static String Endian  = "b";
	public final static int EndianLen  = 1;
	public final static int FlagsLen   = 8;

	public double version;
	public long flags;
	public boolean isDataCentric;
	
	public int RecordSz; // in bytes
				
	public Header(double _version, long _flags) {
		version = _version;
		flags = _flags;
		isDataCentric = (flags == 1);

		RecordSz = Constants.SIZEOF_LONG   // time stamp
					+ Constants.SIZEOF_INT; // call path id
		if (isDataCentric) {
			RecordSz += Constants.SIZEOF_INT; // metric id
		}
	}
}
