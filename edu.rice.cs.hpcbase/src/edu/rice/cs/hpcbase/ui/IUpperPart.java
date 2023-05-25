package edu.rice.cs.hpcbase.ui;

public interface IUpperPart 
{
	String getTitle ();
	
	void setInput(Object input);
	
	boolean hasEqualInput(Object input);
	
	void setMarker(int lineNumber);
	
	void setFocus();
}
