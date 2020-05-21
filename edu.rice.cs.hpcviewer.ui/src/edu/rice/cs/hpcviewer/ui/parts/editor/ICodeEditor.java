package edu.rice.cs.hpcviewer.ui.parts.editor;

public interface ICodeEditor 
{
	public void setData  (Object obj);
	public void setTitle (String title);
	public void setMarker(int lineNumber);
}
