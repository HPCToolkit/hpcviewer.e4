package edu.rice.cs.hpcbase.ui;

import org.eclipse.swt.widgets.Composite;

public interface ILowerPart 
{
	
	void createContent(Composite parent);
	
	void setInput(Object input);
	
	Object getInput();
	
	void activate();
}
