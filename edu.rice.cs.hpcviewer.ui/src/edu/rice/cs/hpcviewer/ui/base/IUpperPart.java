package edu.rice.cs.hpcviewer.ui.base;

public interface IUpperPart 
{
	public String getTitle ();
	
	public void setInput(Object input);
	public boolean hasEqualInput(Object input);
	
	public void setMarker(int lineNumber);
}
