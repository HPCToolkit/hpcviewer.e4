package edu.rice.cs.hpcviewer.ui.parts.editor;

import edu.rice.cs.hpcviewer.ui.parts.IBasePart;

public interface ICodeEditor extends IBasePart 
{
	public void setTitle (String title);
	public void setMarker(int lineNumber);
}
