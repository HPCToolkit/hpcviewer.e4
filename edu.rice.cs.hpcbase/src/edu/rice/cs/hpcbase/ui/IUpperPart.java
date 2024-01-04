package edu.rice.cs.hpcbase.ui;

import edu.rice.cs.hpcbase.IBaseInput;

public interface IUpperPart 
{
	String getTitle ();
	
	void setInput(IBaseInput input);
	
	boolean hasEqualInput(IBaseInput input);
	
	void setMarker(int lineNumber);
	
	void setFocus();
}
