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
		int red  = (hash >> 16) & 0xFF;
		int green = (hash >> 8) & 0xFF;
		int blue  = hash & 0xFF;
		
		RGB rgb = new RGB(red, green, blue);
		return rgb;
	}
}
