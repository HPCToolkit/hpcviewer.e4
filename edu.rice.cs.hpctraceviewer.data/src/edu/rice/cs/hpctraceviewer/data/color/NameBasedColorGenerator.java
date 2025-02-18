// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.data.color;

import org.apache.commons.codec.digest.MurmurHash3;
import org.eclipse.swt.graphics.RGB;


/****************************************
 * 
 * Class to create a color based on the procedure name
 *
 ****************************************/
public class NameBasedColorGenerator implements IColorGenerator 
{
	private static final int MAX_COLOR = 235;

	@Override
	public RGB createColor(String procedureName) {
		// use deprecated hash32 method because it's supported by
		// both apache codec 1.13 and 1.15.
		// Maven by default will download 1.13, while Eclipse downloads
		// 1.15 version :-(
		
		int hash = MurmurHash3.hash32x86(procedureName.getBytes());
		//int hash = procedureName.hashCode();
		
		// convert the lower 24 bits to color
		// the highest 8 bits can be used by spreading into 3 parts
		// into red (3 bits), green (3 bits) and blue (2 bits)
		int overflow1 = (hash >> 29) & 0x7;
		int overflow2 = (hash >> 26) & 0x7;
		int overflow3 = (hash >> 24) & 0x3;
		
		int red   = Math.min((hash >> 16) & 0xFF | overflow1, MAX_COLOR);
		int green = Math.min((hash >> 8) & 0xFF  | overflow2, MAX_COLOR);
		int blue  = Math.min( hash & 0xFF        | overflow3, MAX_COLOR); 
		
		RGB rgb = new RGB(red, green, blue);
		return rgb;
	}
}
