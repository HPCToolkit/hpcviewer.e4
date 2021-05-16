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

	@Override
	public RGB createColor(String procedureName) {
		// use deprecated hash32 method because it's supported by
		// both apache codec 1.13 and 1.15.
		// Maven by default will download 1.13, while Eclipse downloads
		// 1.15 version :-(
		
		int hash = MurmurHash3.hash32(procedureName.getBytes());
		//int hash = procedureName.hashCode();
		
		// convert the lower 24 bits to color
		// the highest 8 bits can be used by spreading into 3 parts
		// into red (3 bits), green (3 bits) and blue (2 bits)
		int overflow1 = (hash >> 29) & 0x7;
		int overflow2 = (hash >> 26) & 0x7;
		int overflow3 = (hash >> 24) & 0x3;
		
		int red  = (hash >> 16) & 0xFF | overflow1;
		int green = (hash >> 8) & 0xFF | overflow2;
		int blue  = hash & 0xFF        | overflow3; 
		
		RGB rgb = new RGB(red, green, blue);
		return rgb;
	}
}
