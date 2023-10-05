package edu.rice.cs.hpcbase;

public interface IEditorInput extends IEditorViewerInput
{
	
	String getContent();
	
	int getLine();
}
