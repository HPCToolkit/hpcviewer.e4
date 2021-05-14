package edu.rice.cs.hpctraceviewer.data.color;

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

		int hash = procedureName.hashCode();
		int red  = (hash >> 16) & 0xFF;
		int green = (hash >> 8) & 0xFF;
		int blue  = hash & 0xFF;
		
		RGB rgb = new RGB(red, green, blue);
		return rgb;
	}
}
