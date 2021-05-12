package edu.rice.cs.hpctraceviewer.data.color;

import org.eclipse.swt.graphics.RGB;

public interface IColorGenerator 
{
	RGB createColor(String procedureName);
}
